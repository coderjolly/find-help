# SOEN 6841 (Software Engineering Team Project)

This project will help to get a taste of software project management skills first-hand. One has to follow the Agile development approach; use supporting project management tools of their choice for tracking progress against the plan, and communicate about the progress (Jira, Asana, etc.). Because of the short span of this project, one is not expected to deliver a marketable product, but the result should be at least a compelling middle-fidelity prototype that could serve as the basis for defining a real product.

# Depression Data Collection Portal (Backend)

The objective of this project is to provide an app or a website to help patients, counselors and doctors to have access faster to the medical system. In this system, patients are able to register and perform a selfassessment test in order to get help from a doctor or a counselor. After registering and performing the selfassessment test, these data will be communicated to a counselor for consultation. A counselor will determine to consult more with the patient by giving him/her appointment, or assign the patient to a doctor, or not accepting the patient because the symptoms are not serious. 

After assigning a patient to a doctor, doctor can go over the patient’s information including the self-assessment test and provide an appointment to the patient. If there is no need to see the patient, the doctor can reject the patient. In either case, the patient
will be notified. All the data will be stored in a database in order to provide different type of reports for the management.
The following are the capabilities of each user:

- Patient:
    1. Registration (Full name, address, date of birth, address, phone number, email address)
    2. Fill their self-assessment form
    3. Cancel their self-assessment form
    4. See their appointment with a counselor or a doctor
    5. Accept or Cancel their appointment

- Counselor:
    1. Registration (Full name, address, date of birth, address, phone number, email address, counselor registration number)
    2. List of their assigned patients
    3. Self-assessment results of their respectivce assigned patients
    4. Appointments with their patients
    5. Assigning a patient to a Doctor
    6. Rejecting an assigned patient
    7. Modifying their appointments with the patients

- Doctor:
    1. Registration (Full name, address, date of birth, address, phone number, email address, doctor registration number)
    2. List of their assigned patients
    3. Self-assessment results for their respective assigned patients
    4. Appointments with their Patients
    5. Rejecting an assigned patient
    6. Modifying their appointments with the patients

- Manager:
    1. Accept or Reject a doctor 
    2. Accept or Reject a counselor
    3. Add or Remove a patient
    4. Make Report: Number of Patients (Day, Week, Month wise)

[//]: <> (Credits)
