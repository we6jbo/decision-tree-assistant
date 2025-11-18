# Decision Tree Assistant (Android)

This app sends questions to a Raspberry Pi "Decision Tree" AI via email,
then polls Gmail for the reply.

## Flow

1. User enters a question and taps **Send to Raspberry Pi**.
2. App generates a Request ID (RQ) based on the current time, e.g. `2025-11-16-174520`.
3. App sends an email:
   - To: `master@we6jbobbs.org`
   - Subject: `dt-in RQ:<requestId>`
4. Raspberry Pi processes the email and replies:
   - To: `we6jbo+decisiontree@gmail.com`
   - Subject: `dt-out RQ:<requestId>`
5. App calls Gmail API and searches for:
   - `to:we6jbo+decisiontree@gmail.com subject:"dt-out RQ:<requestId>"`

If found, the body is shown in the app as the AI's answer.
