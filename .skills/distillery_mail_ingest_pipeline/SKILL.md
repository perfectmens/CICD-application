---
name: "distillery-mail-ai-ingest"
description: "Deploy an email listener daemon that extracts distillery Excel sheets, runs the RandomForest batch classifier, and broadcasts real-time dashboard updates."
---

# Distillery Email-to-AI Ingestion Blueprint (`distillery-mail-ai-ingest`)

This skill contains the complete code templates, architecture flow, and integration parameters to deploy the automated email ingestion pipeline.

---

## 1. Mail Ingest Daemon (`scripts/email_ingest_service.py`)

A python daemon to continuously poll the IMAP inbox for Excel attachments.

```python
import imaplib
import email
import os
import time
import pandas as pd
import joblib
import requests

# Server Credentials (replace with real IMAP config)
IMAP_SERVER = "imap.factory.com"
EMAIL_USER = "disttool@factory.com"
EMAIL_PASS = "secure_password"
API_URL = "http://localhost:5000/api/distillery/logs"

def check_mail():
    try:
        mail = imaplib.IMAP4_SSL(IMAP_SERVER)
        mail.login(EMAIL_USER, EMAIL_PASS)
        mail.select("inbox")
        
        # Search for unread emails from authorized senders
        status, response = mail.search(None, '(UNSEEN)')
        mail_ids = response[0].split()
        
        for num in mail_ids:
            status, data = mail.fetch(num, '(RFC822)')
            msg = email.message_from_bytes(data[0][1])
            
            # Check sender validation
            sender = msg.get("From")
            subject = msg.get("Subject") or ""
            
            if "distillery" in subject.lower():
                for part in msg.walk():
                    if part.get_content_maintype() == 'multipart':
                        continue
                    if part.get('Content-Disposition') is None:
                        continue
                        
                    filename = part.get_filename()
                    if filename and filename.endswith('.xlsx'):
                        filepath = os.path.join("/tmp/inbox_attachments", filename)
                        os.makedirs(os.path.dirname(filepath), exist_ok=True)
                        with open(filepath, 'wb') as f:
                            f.write(part.get_payload(decode=True))
                        
                        # Process file with AI
                        process_and_send(filepath)
            
            # Mark as read
            mail.store(num, '+FLAGS', '\\Seen')
            
        mail.close()
        mail.logout()
    except Exception as e:
        print(f"IMAP Error: {e}")

def process_and_send(filepath):
    # 1. Parse Excel
    df_data = pd.read_excel(filepath, sheet_name="Synthetic Data", skiprows=5, header=None)
    df_data = df_data.dropna(subset=[2]).copy()
    row = df_data[df_data[2] != "Date"].iloc[-1] # Fetch latest run row
    
    # 2. Extract 9 features
    features = {
        "feeding_hours": float(row[8]),
        "retention_hours": float(row[9]),
        "setup_sg": float(row[28]),
        "final_sg": float(row[29]),
        "sg_diff": float(row[30]),
        "setup_alc": float(row[31]),
        "final_alc": float(row[32]),
        "setup_ph": float(row[35]),
        "final_ph": float(row[36])
    }
    
    # 3. Load Model and Predict
    model = joblib.load("batch_quality_classifier.joblib")
    input_df = pd.DataFrame([features])
    pred = int(model.predict(input_df)[0])
    alert_label = ["Low Alcohol", "Normal", "Golden Batch"][pred]
    
    # 4. Assemble API payload
    payload = {
        "date": str(row[2]).split()[0],
        "fermenter": int(row[4]),
        "pf": str(row[3]).strip(),
        "feedingHours": float(row[8]),
        "retentionHours": float(row[9]),
        "setupSg": float(row[28]),
        "finalSg": float(row[29]),
        "setupPh": float(row[35]),
        "finalPh": float(row[36]),
        "alert": alert_label,
        "actualProduction": float(row[45]),
        "feEfficiency": float(row[46]),
        "ethanolPct": float(row[68])
    }
    
    # Send to backend
    requests.post(API_URL, json=payload)

if __name__ == "__main__":
    while True:
        check_mail()
        time.sleep(30) # Poll every 30s
```

---

## 2. Backend Broadcast Controller (`app.py` integration)

FastAPI/Flask API receiver to commit to database and broadcast to the dashboard.

```python
from flask import Flask, request, jsonify
# Assume sse stream instance exists as sse_manager

@app.route('/api/distillery/logs', methods=['POST'])
def add_distillery_log():
    data = request.json
    
    # 1. Write to DB (Mock logic)
    db.insert_record(data)
    
    # 2. Broadcast via SSE
    sse_manager.broadcast({
        "event": "new_distillery_batch",
        "data": {
            "date": data["date"],
            "fermenter": data["fermenter"],
            "pf": data["pf"],
            "alert": data["alert"],
            "actualProduction": data["actualProduction"],
            "feEfficiency": data["feEfficiency"],
            "ethanolPct": data["ethanolPct"]
        }
    })
    
    return jsonify({"status": "success", "message": "Log ingested and broadcasted"})
```

---

## 3. Flutter Client Integration (`sse_listener.dart`)

Client-side listener to trigger notifications and dynamic table refresh.

```dart
// SSE listener subscription inside dashboard_screen.dart
void _setupEmailIngestListener() {
  final sseUrl = Uri.parse("http://localhost:5000/stream");
  
  // Connect to SSE stream
  html.EventSource(sseUrl.toString()).onMessage.listen((event) {
    final payload = jsonDecode(event.data);
    
    if (payload['event'] == 'new_distillery_batch') {
      final batchData = payload['data'];
      
      // 1. Show dynamic glassmorphism notification toast
      _showIngestNotification(
        batchData['date'],
        batchData['fermenter'],
        batchData['alert']
      );
      
      // 2. Append to ledger state variables to reload grid & charts
      setState(() {
        historicalOperationsDataset.insert(0, HistoricalOperationRecord(
          date: batchData['date'],
          fermenter: batchData['fermenter'],
          pf: batchData['pf'],
          alert: batchData['alert'],
          actualProduction: batchData['actualProduction'],
          feEfficiency: batchData['feEfficiency'],
          ethanolPct: batchData['ethanolPct'],
          // fill rest of fields with defaults
        ));
      });
    }
  });
}
```
