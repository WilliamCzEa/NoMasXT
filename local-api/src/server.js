const cors = require("cors");
const dotenv = require("dotenv");
const express = require("express");
const mysql = require("mysql2/promise");

dotenv.config();

const app = express();
const port = Number(process.env.PORT || 3000);

app.use(cors());
app.use(express.json());

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
});

function createFolio() {
  return Math.random().toString(36).slice(2, 7).toUpperCase();
}

function normalizeMsisdn(value) {
  if (typeof value !== "string") return "";
  const trimmed = value.trim();

  if (trimmed.startsWith("+")) {
    return `+${trimmed.slice(1).replace(/\D/g, "")}`;
  }

  if (trimmed.startsWith("00")) {
    return `+${trimmed.slice(2).replace(/\D/g, "")}`;
  }

  return trimmed.replace(/\D/g, "");
}

function isValidE164(msisdn) {
  return /^\+\d{8,15}$/.test(msisdn);
}

app.get("/health", async (_req, res) => {
  const [rows] = await pool.query("SELECT 1 AS ok");
  res.json({ ok: rows[0].ok === 1 });
});

app.post("/services/1.0/extortion/ReporterNumber", async (req, res) => {
  const { appInfo, userId, reportedMsisdn, label } = req.body || {};
  const msisdn = normalizeMsisdn(reportedMsisdn);

  // Normalizacion internacional: la API local solo acepta E.164, como +525510522522.
  if (!isValidE164(msisdn)) {
    return res.status(400).json({
      result: {
        resultCode: 400,
        resultMessage: "reportedMsisdn must be E.164, for example +525510522522",
      },
      blockedNumber: null,
    });
  }

  const connection = await pool.getConnection();

  try {
    await connection.beginTransaction();

    const folio = createFolio();
    const reportLabel = label || "Reporte local";
    const os = appInfo?.os || "AND";
    const versionOs = appInfo?.versionOs || "";
    const skuApp = appInfo?.skuApp || "";
    let userRegistryId = Number(userId);
    if (!userRegistryId) {
      const [userRows] = await connection.execute(
        "SELECT id FROM user_registry ORDER BY id ASC LIMIT 1"
      );
      userRegistryId = userRows[0]?.id;
    }

    if (!userRegistryId) {
      throw new Error("No user_registry row found for local report test");
    }

    // blocked_numbers tiene msisdn UNIQUE. Si ya existe, incrementamos hits.
    await connection.execute(
      `
        INSERT INTO blocked_numbers (msisdn, hits, tag_id, status, folio)
        VALUES (?, 1, 1, 1, ?)
        ON DUPLICATE KEY UPDATE
          hits = hits + 1,
          status = 1,
          folio = COALESCE(folio, VALUES(folio)),
          modification_status = 1
      `,
      [msisdn, folio]
    );

    // reported_numbers guarda el evento individual de reporte.
    await connection.execute(
      `
        INSERT INTO reported_numbers
          (user_registry_id, msisdn, label, os, version_os, sku_app, type, status)
        VALUES
          (?, ?, ?, ?, ?, ?, 2, 1)
      `,
      [userRegistryId, msisdn, reportLabel, os, versionOs, skuApp]
    );

    const [blockedRows] = await connection.execute(
      `
        SELECT msisdn, first_date, status, folio
        FROM blocked_numbers
        WHERE msisdn = ?
        LIMIT 1
      `,
      [msisdn]
    );

    await connection.commit();

    const blockedNumber = blockedRows[0];

    res.json({
      result: {
        resultCode: 200,
        resultMessage: "OK",
      },
      blockedNumber: {
        date: blockedNumber.first_date,
        folio: blockedNumber.folio || folio,
        label: reportLabel,
        phoneNumber: blockedNumber.msisdn,
        status: String(blockedNumber.status),
      },
    });
  } catch (error) {
    await connection.rollback();
    console.error("ReporterNumber error:", error);

    res.status(500).json({
      result: {
        resultCode: 500,
        resultMessage: error.message,
      },
      blockedNumber: null,
    });
  } finally {
    connection.release();
  }
});

app.post("/services/1.0/extortion/DeleteReportedNumber", async (req, res) => {
  const { userId, reportedMsisdn } = req.body || {};
  const msisdn = normalizeMsisdn(reportedMsisdn);

  // El borrado usa la misma normalizacion que el alta para encontrar la fila exacta.
  if (!isValidE164(msisdn)) {
    return res.status(400).json({
      result: {
        resultCode: 400,
        resultMessage: "reportedMsisdn must be E.164, for example +525510522522",
      },
    });
  }

  const connection = await pool.getConnection();

  try {
    await connection.beginTransaction();

    let userRegistryId = Number(userId);
    if (!userRegistryId) {
      const [userRows] = await connection.execute(
        "SELECT id FROM user_registry ORDER BY id ASC LIMIT 1"
      );
      userRegistryId = userRows[0]?.id;
    }

    if (!userRegistryId) {
      throw new Error("No user_registry row found for local delete test");
    }

    // No borramos fisicamente: dejamos historial y ocultamos el reporte del usuario.
    await connection.execute(
      `
        UPDATE reported_numbers
        SET status = 0
        WHERE msisdn = ?
          AND user_registry_id = ?
          AND status = 1
      `,
      [msisdn, userRegistryId]
    );

    const [activeRows] = await connection.execute(
      `
        SELECT COUNT(*) AS activeReports
        FROM reported_numbers
        WHERE msisdn = ?
          AND status = 1
      `,
      [msisdn]
    );

    if (Number(activeRows[0]?.activeReports || 0) === 0) {
      await connection.execute(
        `
          UPDATE blocked_numbers
          SET status = 0,
              modification_status = 1
          WHERE msisdn = ?
        `,
        [msisdn]
      );
    }

    await connection.commit();

    res.json({
      result: {
        resultCode: 200,
        resultMessage: "OK",
      },
    });
  } catch (error) {
    await connection.rollback();
    console.error("DeleteReportedNumber error:", error);

    res.status(500).json({
      result: {
        resultCode: 500,
        resultMessage: error.message,
      },
    });
  } finally {
    connection.release();
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`NoMasXT local API listening on http://0.0.0.0:${port}`);
});
