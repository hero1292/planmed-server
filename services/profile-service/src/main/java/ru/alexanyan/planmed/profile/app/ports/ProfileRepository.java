package ru.alexanyan.planmed.profile.app.ports;

import java.util.Optional;

public interface ProfileRepository {
    Optional<PatientProfileView> findPatientByUserId(String userId);
    Optional<DoctorProfileView> findDoctorByUserId(String userId);

    record PatientProfileView(String userId, String fullName, String contactEmail) {}
    record DoctorProfileView(String userId, String fullName, String specialty) {}
}
