package org.example.bookingsystemapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    @OneToMany(mappedBy = "patient", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<DoctorSchedule> doctorSchedule = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ConfirmationMethod confirmationMethod;
}
