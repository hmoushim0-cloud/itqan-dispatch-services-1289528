# تطبيق حارس الصحة الذكي — رفيق المراهقين الموثوق (itqan-dispatch-services-1289528)
> ذكاء اصطناعي مدعوم بـ RAG مع حماية NFC و GPS فورية

- **Project ID**: #1289528
- **Commercial Archetype**: services
- **Bound Hardware Peripheral**: Mada Contactless EMV L2 NFC + GPS Dispatch
- **Package Name**: sa.factory.itqandispatchservi
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons and Clean Typography)

---

## Production Domain Engines (Iteration 91)

### 1. Mada Contactless EMV Level 2 Engine (`MadaContactlessEmvL2Engine.kt`)
- **ISO 7816-4 APDU Command Generation**:
  - `SELECT PPSE` (2PAY.SYS.DDF01) with Le=0x00.
  - `SELECT AID` for Saudi Mada primary (`A0000002281010`) and secondary schemes.
  - `GET PROCESSING OPTIONS` (GPO) with PDOL parameter construction.
- **BER-TLV Parser**:
  - Extracts Application Transaction Counter (ATC Tag 0x9F36), Application Cryptogram (AC Tag 0x9F26), and Application Label (Tag 0x50).
- **SAMA Compliance & CVM Verification**:
  - Contactless Floor Limit rule: Transactions <= 300.00 SAR processed without PIN.
  - Transactions > 300.00 SAR enforce Cardholder Verification Method (CVM: On-Device PIN / Biometrics).
  - SHA-256 session integrity signature generation for SAMA audit compliance.

### 2. Real-Time GPS Emergency Dispatch Engine (`GpsEmergencyDispatchEngine.kt`)
- **Geodesic Haversine & Bearing Calculations**:
  - High-precision distance calculation in kilometers and compass navigation bearing in degrees (0..359).
- **Geofence Safe Zone Monitoring**:
  - Radial perimeter containment verification (e.g. King Fahd Medical City zone).
- **Red Crescent SLA Triage Classification**:
  - `CODE_RED` (Critical SOS, 8-minute maximum SLA response).
  - `CODE_YELLOW` (Urgent Triage, 20-minute SLA response).
  - `CODE_GREEN` (Standard Health Advisory, 60-minute SLA response).
- **Fleet Dispatch Optimization**:
  - Dynamic nearest active ambulance responder allocation with projected ETA and cryptographic token verification.

---

## Verification & Unit Testing

- 10 comprehensive unit tests in `ItqanDispatchRegressionTest.kt`:
  1. `testCheckoutCalculationAndTax`: Subtotal, 15% VAT, and total SAR calculation.
  2. `testSelectPpseApduFormatting`: ISO 7816-4 PPSE APDU command framing.
  3. `testSelectMadaAidApdu`: ISO 7816-4 Mada AID selection APDU.
  4. `testBerTlvParser`: EMV BER-TLV tag parsing for ATC and Cryptogram.
  5. `testMadaContactlessNoPinFloorLimitBelow300`: Verification of no-PIN threshold under 300 SAR.
  6. `testMadaContactlessPinRequiredAbove300`: Verification of mandatory CVM over 300 SAR.
  7. `testMadaContactlessTransactionApprovalAndVat`: Full contactless approval and SHA-256 session digest.
  8. `testGeodesicDistanceAndBearingHaversine`: Geodesic coordinate calculation across Riyadh landmarks.
  9. `testGeofenceSafeZoneDetection`: In-zone and perimeter breach boundary enforcement.
  10. `testSosTriageClassificationAndNearestAmbulanceDispatch`: Emergency triage priority and nearest responder routing.
