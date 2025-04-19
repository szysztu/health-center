package org.example.bookingsystemapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.bookingsystemapp.entities.Specialisation;
import org.example.bookingsystemapp.exception.MissingSpecialisationException;
import org.example.bookingsystemapp.exception.NotFoundException;
import org.example.bookingsystemapp.exception.VersionMismatchException;
import org.example.bookingsystemapp.mapper.DoctorMapper;
import org.example.bookingsystemapp.model.DeleteReferenceDTO;
import org.example.bookingsystemapp.model.DoctorCreateDTO;
import org.example.bookingsystemapp.model.DoctorDTO;
import org.example.bookingsystemapp.repositories.DoctorRepository;
import org.springframework.stereotype.Service;


@Transactional
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    public DoctorDTO createDoctor(DoctorCreateDTO doctorCreateDTO) {
        var doctor = doctorMapper.toDoctor(doctorCreateDTO);

        if (doctorCreateDTO.getSpecialisation() == null) {
            throw new MissingSpecialisationException("Doctor must have specialisation");
        }
        doctor.setSpecialisation(Specialisation.valueOf(doctorCreateDTO.getSpecialisation()));

        doctorRepository.save(doctor);
        return doctorMapper.toDoctorDTO(doctor);
    }

    public DoctorDTO getDoctorById(Long id) {
        return doctorRepository.findById(id).map(doctorMapper::toDoctorDTO)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id " + id));
    }

    public DoctorDTO updateDoctor(DoctorDTO doctorDTO) {
        var doctor = doctorRepository.findById(doctorDTO.getId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id " + doctorDTO.getId()));
        if (!doctorDTO.getVersion().equals(doctor.getVersion())) {
            throw new VersionMismatchException("Version mismatch for user with id " + doctorDTO.getId());
        }

        if (doctorDTO.getEmail() != null) {
            doctor.setEmail(doctorDTO.getEmail());
        }
        if (doctorDTO.getFirstName() != null) {
            doctor.setFirstName(doctorDTO.getFirstName());
        }
        if (doctorDTO.getLastName() != null) {
            doctor.setLastName(doctorDTO.getLastName());
        }
        if (doctorDTO.getPhoneNumber() != null) {
            doctor.setPhoneNumber(doctorDTO.getPhoneNumber());
        }
        if (doctorDTO.getBirthDate() != null) {
            doctor.setBirthDate(doctorDTO.getBirthDate());
        }
        if (doctorDTO.getSpecialisation() != null) {
            doctor.setSpecialisation(Specialisation.valueOf(doctorDTO.getSpecialisation().toUpperCase()));
        }

        var updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorDTO(updatedDoctor);
    }

    public void deleteDoctor(DeleteReferenceDTO deleteReferenceDTO) {
        var doctor = doctorRepository.findById(deleteReferenceDTO.getId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id " + deleteReferenceDTO.getId()));
        if (!deleteReferenceDTO.getVersion().equals(doctor.getVersion())) {
            throw new VersionMismatchException("Version mismatch for doctor with id " + deleteReferenceDTO.getId());
        }
        doctorRepository.deleteById(deleteReferenceDTO.getId());
    }

}
