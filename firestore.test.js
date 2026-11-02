const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const STUDENT_ALICE_UID = "student_alice_1";
const STUDENT_BOB_UID = "student_bob_2";
const SUPER_ADMIN_UID = "super_admin_pres";
const FINANCE_SEC_UID = "finance_sec_1";
const INFO_SEC_UID = "info_sec_1";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function nowTs() {
  return new Date();
}

async function seedAdmin(uid, adminRole, permissions = []) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("admins").doc(uid).set({
      uid,
      email: `${uid}@uaf.edu.pk`,
      fullName: `Admin ${uid}`,
      adminRole,
      designation: adminRole,
      permissions,
      active: true,
      assignedByUid: SUPER_ADMIN_UID,
      societyId: "YES_UAF",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    });
  });
}

test("1. Unauthenticated user cannot read events or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("events").where("published", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(STUDENT_ALICE_UID).get());
});

test("2. Student can register STUDENT account & profile, but CANNOT self-assign ADMIN", async () => {
  const aliceDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();
  const ts = nowTs();

  // Attempt privilege escalation -> MUST FAIL
  await assertFails(
    aliceDb.collection("users").doc(STUDENT_ALICE_UID).set({
      userId: STUDENT_ALICE_UID,
      email: "alice@uaf.edu.pk",
      fullName: "Alice Khan",
      accountType: "ADMIN",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Legitimate student creation -> MUST SUCCEED
  await assertSucceeds(
    aliceDb.collection("users").doc(STUDENT_ALICE_UID).set({
      userId: STUDENT_ALICE_UID,
      email: "alice@uaf.edu.pk",
      fullName: "Alice Khan",
      accountType: "STUDENT",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Attempt forging ACTIVE membership on student profile -> MUST FAIL
  await assertFails(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-1102",
      department: "Department of Entomology",
      semester: "5th",
      membershipStatus: "ACTIVE",
      membershipId: "YES-FORGED-001",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Legitimate student profile with NONE status -> MUST SUCCEED
  await assertSucceeds(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-1102",
      department: "Department of Entomology",
      semester: "5th",
      membershipStatus: "NONE",
      membershipId: "",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("3. PII Isolation: Student cannot read another student's profile or unfiltered list", async () => {
  const ts = nowTs();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("students").doc(STUDENT_BOB_UID).set({
      studentUid: STUDENT_BOB_UID,
      fullName: "Bob Tariq",
      email: "bob@uaf.edu.pk",
      registrationNumber: "2023-ag-2290",
      department: "Entomology",
      semester: "7th",
      membershipStatus: "ACTIVE",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const aliceDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();
  await assertFails(aliceDb.collection("students").doc(STUDENT_BOB_UID).get());
  await assertFails(aliceDb.collection("students").get());
});

test("4. Email spoofing guard: unverified bootstrapped admin email is rejected", async () => {
  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "abubakar.offical227@gmail.com",
      email_verified: false,
    })
    .firestore();

  await assertFails(
    spoofDb.collection("admins").doc("new_admin").set({
      uid: "new_admin",
      email: "new@uaf.edu.pk",
      fullName: "Spoof Admin",
      adminRole: "EVENT_SECRETARY",
      designation: "Event Secretary",
      permissions: ["MANAGE_EVENTS"],
      active: true,
      assignedByUid: "spoof_uid",
      societyId: "YES_UAF",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  const verifiedOwnerDb = testEnv
    .authenticatedContext(SUPER_ADMIN_UID, {
      email: "abubakar.offical227@gmail.com",
      email_verified: true,
    })
    .firestore();

  await assertSucceeds(
    verifiedOwnerDb.collection("admins").doc(FINANCE_SEC_UID).set({
      uid: FINANCE_SEC_UID,
      email: "finance@uaf.edu.pk",
      fullName: "Bilal Ahmad",
      adminRole: "FINANCE_SECRETARY",
      designation: "Finance Secretary",
      permissions: ["MANAGE_FINANCE"],
      active: true,
      assignedByUid: SUPER_ADMIN_UID,
      societyId: "YES_UAF",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("5. Finance records strictly restricted to Finance Secretary & Super Admin", async () => {
  await seedAdmin(FINANCE_SEC_UID, "FINANCE_SECRETARY", ["MANAGE_FINANCE"]);
  await seedAdmin(INFO_SEC_UID, "INFORMATION_SECRETARY", ["MANAGE_ANNOUNCEMENTS", "MANAGE_GALLERY"]);

  const financeDb = testEnv.authenticatedContext(FINANCE_SEC_UID).firestore();
  const infoSecDb = testEnv.authenticatedContext(INFO_SEC_UID).firestore();
  const studentDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();

  const txPayload = {
    title: "Annual Dinner Sponsorship",
    type: "INCOME",
    category: "SPONSORSHIP",
    amount: 45000,
    transactionDate: "2026-10-30",
    recordedByUid: FINANCE_SEC_UID,
    recordedByName: "Finance Secretary",
    societyId: "YES_UAF",
    createdAt: nowTs(),
    updatedAt: nowTs(),
  };

  // Finance Secretary can create and list finance transactions
  await assertSucceeds(financeDb.collection("finance_transactions").doc("tx_1").set(txPayload));
  await assertSucceeds(
    financeDb.collection("finance_transactions").where("societyId", "==", "YES_UAF").get()
  );

  // Information Secretary & Student CANNOT read or write finance ledger
  await assertFails(
    infoSecDb.collection("finance_transactions").where("societyId", "==", "YES_UAF").get()
  );
  await assertFails(infoSecDb.collection("finance_transactions").doc("tx_1").get());
  await assertFails(studentDb.collection("finance_transactions").doc("tx_1").get());
});

test("6. Shadow Update / Ghost Field rejection & Audit Log immutability", async () => {
  await seedAdmin(INFO_SEC_UID, "INFORMATION_SECRETARY", ["MANAGE_ANNOUNCEMENTS"]);
  const infoSecDb = testEnv.authenticatedContext(INFO_SEC_UID).firestore();
  const ts = nowTs();

  // Ghost field in announcement -> MUST FAIL
  await assertFails(
    infoSecDb.collection("announcements").doc("ann_1").set({
      title: "General Body Meeting",
      message: "Meeting at 2 PM in Entomology Hall",
      category: "MEETING",
      targetGroup: "ALL_STUDENTS",
      priority: "IMPORTANT",
      authorName: "Info Sec",
      authorUid: INFO_SEC_UID,
      published: true,
      societyId: "YES_UAF",
      ghostField: "malicious",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid audit log creation -> SUCCEEDS, update/delete -> FAILS
  await assertSucceeds(
    infoSecDb.collection("audit_logs").doc("log_1").set({
      actorUid: INFO_SEC_UID,
      actorName: "Info Sec",
      actorRole: "INFORMATION_SECRETARY",
      action: "PUBLISHED_ANNOUNCEMENT",
      targetSummary: "General Body Meeting",
      societyId: "YES_UAF",
      createdAt: ts,
    })
  );
  await assertFails(infoSecDb.collection("audit_logs").doc("log_1").delete());

  // Super Admin CAN delete audit logs in the dedicated Admin Activity section
  const superAdminDb = testEnv
    .authenticatedContext(SUPER_ADMIN_UID, {
      email: "abubakar.offical227@gmail.com",
      email_verified: true,
    })
    .firestore();
  await assertSucceeds(superAdminDb.collection("audit_logs").doc("log_1").delete());
});

test("7. New Executive & Associate roles (Senior VP, Councillor, Joint Sec, IT & Media Cell, Press Sec) and section deletes", async () => {
  const ts = nowTs();
  const superAdminDb = testEnv
    .authenticatedContext(SUPER_ADMIN_UID, {
      email: "abubakar.offical227@gmail.com",
      email_verified: true,
    })
    .firestore();

  // Super Admin can appoint Senior Vice President, Councillor, IT & Media Cell, Press Secretary, and Associates
  for (const roleKey of [
    "SENIOR_VICE_PRESIDENT",
    "ASSOCIATE_SENIOR_VP",
    "COUNCILLOR",
    "ASSOCIATE_COUNCILLOR",
    "JOINT_SECRETARY",
    "ASSOCIATE_JOINT_SECRETARY",
    "IT_MEDIA_CELL",
    "ASSOCIATE_IT_MEDIA_CELL",
    "PRESS_SECRETARY",
    "ASSOCIATE_PRESS_SECRETARY",
    "CONVENER",
    "ADVISORY_BOARD",
  ]) {
    await assertSucceeds(
      superAdminDb.collection("admins").doc(`adm_${roleKey.toLowerCase()}`).set({
        uid: `adm_${roleKey.toLowerCase()}`,
        email: `${roleKey.toLowerCase()}@uaf.edu.pk`,
        fullName: `Officer ${roleKey}`,
        adminRole: roleKey,
        designation: roleKey.replace(/_/g, " "),
        permissions: ["VIEW_REPORTS"],
        active: true,
        assignedByUid: SUPER_ADMIN_UID,
        societyId: "YES_UAF",
        createdAt: ts,
        updatedAt: ts,
      })
    );
  }

  // IT & Media Cell can publish and delete gallery items
  const mediaDb = testEnv.authenticatedContext("adm_it_media_cell").firestore();
  await assertSucceeds(
    mediaDb.collection("gallery_items").doc("gal_media_1").set({
      title: "YES Media Highlight",
      albumCategory: "Society Activities",
      eventDate: "2026-10-31",
      approved: true,
      uploadedByUid: "adm_it_media_cell",
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(mediaDb.collection("gallery_items").doc("gal_media_1").delete());
});

test("8. Profile Lock: Approved student profile is locked against student edits until unlocked by Administrator", async () => {
  await seedAdmin("convener_1", "CONVENER", ["MANAGE_STUDENTS", "MANAGE_MEMBERSHIP"]);
  const aliceDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();
  const convenerDb = testEnv.authenticatedContext("convener_1").firestore();
  const ts = nowTs();

  // Student registers initial unlocked profile with uploaded student card & degree level
  await assertSucceeds(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Ayesha Noor",
      email: "ayesha@uaf.edu.pk",
      registrationNumber: "2023-ag-5501",
      degreeLevel: "BSc (Hons) Agriculture",
      department: "Department of Entomology",
      semester: "5th Semester",
      phone: "0300-1122334",
      photoUri: "data:image/jpeg;base64,abc123",
      studentCardUri: "data:image/jpeg;base64,card123",
      position: "Member",
      profileLocked: false,
      membershipStatus: "NONE",
      membershipId: "",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // While unlocked, student CAN edit their own phone
  await assertSucceeds(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).update({
      phone: "0300-9988776",
      updatedAt: nowTs(),
    })
  );

  // Convener approves student, issues ID card, and locks profile
  await assertSucceeds(
    convenerDb.collection("students").doc(STUDENT_ALICE_UID).update({
      membershipStatus: "ACTIVE",
      membershipId: "YES-2026-5501",
      position: "Active Member",
      profileLocked: true,
      updatedAt: nowTs(),
    })
  );

  // Once locked, student CANNOT edit their profile OR unlock themselves -> MUST FAIL
  await assertFails(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).update({
      fullName: "Unauthorized Rename",
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).update({
      profileLocked: false,
      updatedAt: nowTs(),
    })
  );

  // Convener unlocks profile -> student CAN edit again
  await assertSucceeds(
    convenerDb.collection("students").doc(STUDENT_ALICE_UID).update({
      profileLocked: false,
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).update({
      semester: "6th Semester",
      updatedAt: nowTs(),
    })
  );
});

test("9. Finance Account Details: Finance Admin manages payment_accounts; Students query active accounts only", async () => {
  await seedAdmin(FINANCE_SEC_UID, "FINANCE_SECRETARY", ["MANAGE_FINANCE"]);
  const financeDb = testEnv.authenticatedContext(FINANCE_SEC_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();
  const ts = nowTs();

  const acctPayload = {
    accountType: "Bank Account",
    bankOrWalletName: "HBL UAF Branch",
    accountTitle: "Young Entomologist Society UAF",
    accountNumber: "01427901234503",
    ibanOrBranch: "PK36HABB0001427901234503",
    instructions: "Attach deposit slip screenshot when registering.",
    active: true,
    updatedByUid: FINANCE_SEC_UID,
    societyId: "YES_UAF",
    createdAt: ts,
    updatedAt: ts,
  };

  // Student CANNOT create or delete payment account details
  await assertFails(aliceDb.collection("payment_accounts").doc("acct_1").set(acctPayload));

  // Finance Secretary CAN create payment account details
  await assertSucceeds(financeDb.collection("payment_accounts").doc("acct_1").set(acctPayload));

  // Student CAN query active payment accounts, but CANNOT run unfiltered query if inactive exists
  await assertSucceeds(
    aliceDb.collection("payment_accounts").where("active", "==", true).get()
  );

  await assertSucceeds(
    financeDb.collection("payment_accounts").doc("acct_2").set({
      ...acctPayload,
      active: false,
    })
  );
  await assertFails(aliceDb.collection("payment_accounts").get());

  // Finance Secretary CAN delete payment account
  await assertSucceeds(financeDb.collection("payment_accounts").doc("acct_2").delete());
});

test("10. Student Verification (VERIFIED/REJECTED), ID Card Issue/Revoke, Whole Data Purge & Account Block", async () => {
  await seedAdmin("convener_admin", "CONVENER", ["MANAGE_STUDENTS", "MANAGE_MEMBERSHIP"]);
  const convenerDb = testEnv.authenticatedContext("convener_admin").firestore();
  const aliceDb = testEnv.authenticatedContext(STUDENT_ALICE_UID).firestore();
  const ts = nowTs();

  // Student onboards with NOT_ISSUED idCardStatus; CANNOT forge ISSUED
  await assertFails(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-7710",
      department: "Department of Entomology",
      semester: "5th Semester",
      membershipStatus: "NONE",
      idCardStatus: "ISSUED",
      membershipId: "",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(STUDENT_ALICE_UID).set({
      userId: STUDENT_ALICE_UID,
      email: "alice@uaf.edu.pk",
      fullName: "Alice Khan",
      accountType: "STUDENT",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-7710",
      department: "Department of Entomology",
      semester: "5th Semester",
      membershipStatus: "NONE",
      idCardStatus: "NOT_ISSUED",
      membershipId: "",
      active: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("membership_applications").doc("app_alice_1").set({
      studentUid: STUDENT_ALICE_UID,
      studentName: "Alice Khan",
      studentEmail: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-7710",
      department: "Department of Entomology",
      semester: "5th Semester",
      motivation: "Active participation in entomology field research.",
      status: "PENDING",
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Admin marks application VERIFIED and issues ID card
  await assertSucceeds(
    convenerDb.collection("membership_applications").doc("app_alice_1").update({
      status: "VERIFIED",
      assignedMembershipId: "YES-2026-7710",
      reviewedByUid: "convener_admin",
      reviewNotes: "Verified university card.",
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    convenerDb.collection("students").doc(STUDENT_ALICE_UID).update({
      membershipStatus: "VERIFIED",
      idCardStatus: "ISSUED",
      membershipId: "YES-2026-7710",
      position: "General Member",
      profileLocked: true,
      updatedAt: nowTs(),
    })
  );

  // Admin revokes ID card
  await assertSucceeds(
    convenerDb.collection("students").doc(STUDENT_ALICE_UID).update({
      idCardStatus: "REVOKED",
      updatedAt: nowTs(),
    })
  );

  // Admin deletes whole student data and blocks student account
  await assertSucceeds(convenerDb.collection("membership_applications").doc("app_alice_1").delete());
  await assertSucceeds(convenerDb.collection("students").doc(STUDENT_ALICE_UID).delete());
  await assertSucceeds(convenerDb.collection("users").doc(STUDENT_ALICE_UID).delete());
  await assertSucceeds(
    convenerDb.collection("blocked_accounts").doc(STUDENT_ALICE_UID).set({
      userId: STUDENT_ALICE_UID,
      email: "alice@uaf.edu.pk",
      fullName: "Alice Khan",
      registrationNumber: "2023-ag-7710",
      reason: "Violation of society code of conduct.",
      blockedByUid: "convener_admin",
      blockedByName: "Convener",
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Blocked student CAN read own block notice to see why login was denied
  await assertSucceeds(aliceDb.collection("blocked_accounts").doc(STUDENT_ALICE_UID).get());

  // Blocked student CANNOT unblock themselves, re-register user/student profile, or read events
  await assertFails(aliceDb.collection("blocked_accounts").doc(STUDENT_ALICE_UID).delete());
  await assertFails(
    aliceDb.collection("users").doc(STUDENT_ALICE_UID).set({
      userId: STUDENT_ALICE_UID,
      email: "alice@uaf.edu.pk",
      fullName: "Alice Khan",
      accountType: "STUDENT",
      active: true,
      societyId: "YES_UAF",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    aliceDb.collection("students").doc(STUDENT_ALICE_UID).set({
      studentUid: STUDENT_ALICE_UID,
      fullName: "Alice Khan",
      email: "alice@uaf.edu.pk",
      registrationNumber: "2023-ag-7710",
      department: "Department of Entomology",
      semester: "5th Semester",
      membershipStatus: "NONE",
      idCardStatus: "NOT_ISSUED",
      membershipId: "",
      active: true,
      societyId: "YES_UAF",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );
});

test("13. Administrator (SUPER_ADMIN) is distinct from Society President (PRESIDENT) with strict role boundaries", async () => {
  const ts = nowTs();
  const superAdminDb = testEnv
    .authenticatedContext(SUPER_ADMIN_UID, {
      email: "abubakar.offical227@gmail.com",
      email_verified: true,
    })
    .firestore();

  // Portal Administrator can appoint a separate Society President (PRESIDENT)
  await assertSucceeds(
    superAdminDb.collection("admins").doc("president_zaid").set({
      uid: "president_zaid",
      email: "zaid.president@uaf.edu.pk",
      fullName: "Zaid Ali",
      adminRole: "PRESIDENT",
      designation: "President",
      permissions: [
        "MANAGE_STUDENTS",
        "MANAGE_MEMBERSHIP",
        "MANAGE_EXECUTIVE",
        "MANAGE_EVENTS",
        "MANAGE_REGISTRATIONS",
        "MANAGE_ATTENDANCE",
        "MANAGE_ANNOUNCEMENTS",
        "MANAGE_FINANCE",
        "MANAGE_GALLERY",
        "MANAGE_CERTIFICATES",
        "VIEW_REPORTS",
      ],
      active: true,
      assignedByUid: SUPER_ADMIN_UID,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Society President can manage Executive Body & Events
  const presidentDb = testEnv.authenticatedContext("president_zaid").firestore();
  await assertSucceeds(
    presidentDb.collection("executive_body").doc("exec_pres_1").set({
      fullName: "Zaid Ali",
      designation: "President",
      department: "Department of Entomology",
      tenure: "2026 - 2027",
      email: "zaid.president@uaf.edu.pk",
      photoUri: "",
      displayOrder: 1,
      published: true,
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Society President CANNOT appoint or modify /admins (only Portal Administrator / SUPER_ADMIN can)
  await assertFails(
    presidentDb.collection("admins").doc("unauthorized_sub_admin").set({
      uid: "unauthorized_sub_admin",
      email: "unauth@uaf.edu.pk",
      fullName: "Unauth Admin",
      adminRole: "CONVENER",
      designation: "Convener",
      permissions: ["VIEW_REPORTS"],
      active: true,
      assignedByUid: "president_zaid",
      societyId: "YES_UAF",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

