// Seeds the clinic_system database through the running API.
// Usage: node seed-data.js [baseUrl]   (default http://localhost:1015)
const BASE = process.argv[2] || "http://localhost:1015";

async function call(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(`${method} ${path} -> ${res.status} ${JSON.stringify(data)}`);
  return data;
}

const list = (entity) => call("GET", `/api/${entity}/all`);

async function main() {
  const existing = await list("patient");
  if (existing.length > 0) {
    console.log(`Database already has ${existing.length} patients, nothing seeded.`);
    return;
  }

  // specializations and offices keep their original names / numbers
  for (const name of ["Dermatology", "Neurology", "Cardiology", "Pediatrics"]) {
    await call("POST", "/api/specialization/save", { name });
  }
  for (const officeNumber of ["101", "102", "103"]) {
    await call("POST", "/api/office/save", { officeNumber });
  }
  const specs = Object.fromEntries((await list("specialization")).map((s) => [s.name, s]));
  const offices = Object.fromEntries((await list("office")).map((o) => [o.officeNumber, o]));

  const patients = [
    ["Grace", "Ishimwe", "1001"],
    ["Patrick", "Ishimwe", "1002"],
    ["Emmanuel", "Niyonzima", "1003"],
    ["Josiane", "Mutoni", "1004"],
    ["Thierry", "Bizimana", "1005"],
  ];
  for (const [firstName, lastName, ssNumber] of patients) {
    await call("POST", "/api/patient/save", { firstName, lastName, ssNumber });
  }

  const doctors = [
    ["Marie", "Uwimana", "101", ["Cardiology", "Pediatrics"]],
    ["David", "Nsengiyumva", "102", ["Dermatology"]],
    ["Aline", "Mukeshimana", null, ["Cardiology"]],
  ];
  for (const [firstName, lastName, office, names] of doctors) {
    await call("POST", "/api/doctor/save", {
      firstName,
      lastName,
      office: office ? { id: offices[office].id } : null,
      specializations: names.map((n) => ({ id: specs[n].id })),
    });
  }

  const patientId = Object.fromEntries(
    (await list("patient")).map((p) => [`${p.firstName} ${p.lastName}`, p.id])
  );
  const doctorId = Object.fromEntries((await list("doctor")).map((d) => [d.lastName, d.id]));

  // [patient, doctor last name, date, reason, final status]
  const appointments = [
    // saved and cancelled before Grace's, since a doctor takes one live appointment per day
    ["Patrick Ishimwe", "Uwimana", "2026-10-20T11:00:00", "Blood pressure check", "CANCELLED"],
    ["Grace Ishimwe", "Uwimana", "2026-10-20T09:00:00", "Chest pain follow up", "SCHEDULED"],
    ["Grace Ishimwe", "Nsengiyumva", "2026-10-22T10:00:00", "Skin rash", "CONFIRMED"],
    ["Grace Ishimwe", "Mukeshimana", "2026-11-03T14:00:00", "Child vaccination", "SCHEDULED"],
    ["Emmanuel Niyonzima", "Uwimana", "2026-10-27T08:30:00", "Annual checkup", "COMPLETED"],
    ["Josiane Mutoni", "Uwimana", "2026-11-12T13:00:00", "Palpitations", "CANCELLED"],
    ["Thierry Bizimana", "Mukeshimana", "2026-11-05T09:00:00", "Headache", "SCHEDULED"],
    ["Patrick Ishimwe", "Nsengiyumva", "2026-11-10T11:30:00", "Mole removal", "COMPLETED"],
    ["Thierry Bizimana", "Uwimana", "2026-11-17T10:00:00", "ECG review", "CONFIRMED"],
    ["Josiane Mutoni", "Nsengiyumva", "2026-10-08T15:00:00", "Eczema review", "CANCELLED"],
  ];
  // every appointment is saved as SCHEDULED; a status then moves one step at a time
  const statusSteps = {
    SCHEDULED: [],
    CONFIRMED: ["CONFIRMED"],
    COMPLETED: ["CONFIRMED", "COMPLETED"],
    CANCELLED: ["CANCELLED"],
  };
  for (const [patient, doctor, date, reason, status] of appointments) {
    // only future dates can be booked, so a past one is booked a year later and moved back by the update
    const isPast = new Date(date) < new Date();
    const body = {
      patient: { id: patientId[patient] },
      doctor: { id: doctorId[doctor] },
      appointmentDate: isPast ? date.replace(/^\d{4}/, (year) => String(Number(year) + 1)) : date,
      reason,
    };
    await call("POST", "/api/appointment/save", body);
    const saved = (await list("appointment")).find(
      (a) =>
        a.patient.id === body.patient.id &&
        a.appointmentDate === body.appointmentDate &&
        a.doctor.id === body.doctor.id
    );
    if (isPast) {
      await call("PUT", `/api/appointment/update/${saved.id}`, { ...body, appointmentDate: date });
    }
    for (const step of statusSteps[status]) {
      await call("PATCH", `/api/appointments/${saved.id}/status?status=${step}`);
    }
  }

  console.log("Seeded: 4 specializations, 3 offices, 5 patients, 3 doctors, 10 appointments.");
}

main().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
