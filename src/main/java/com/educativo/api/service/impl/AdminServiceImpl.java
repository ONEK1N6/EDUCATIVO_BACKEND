package com.educativo.api.service.impl;

import com.educativo.api.dto.*;
import com.educativo.api.entity.*;
import com.educativo.api.repository.CreditCardRepository;
import com.educativo.api.repository.RoleRepository;
import com.educativo.api.repository.StudentRepository;
import com.educativo.api.repository.UserRepository;
import com.educativo.api.service.AdminService;
import com.educativo.api.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    @Transactional
    public MessageResponse updateParent(Long id, ParentUpdateRequest request) {
// ... existing code
        User parent = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Parent not found."));

        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setDni(request.getDni());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(parent);
        return new MessageResponse("Parent updated successfully!");
    }

    @Override
    @Transactional
    public MessageResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setDni(request.getDni());
        student.setSection(request.getSection());
        student.setGrade(request.getGrade());
        student.setLevel(request.getLevel());

        studentRepository.save(student);
        return new MessageResponse("Student updated successfully!");
    }

    @Override
    @Transactional
    public MessageResponse registerParent(ParentRegistrationRequest request) {
        if (userRepository.existsByDni(request.getDni())) {
            return new MessageResponse("Error: DNI is already in use!");
        }

        // 1. Generate Username: name.surname (lowercase)
        String username = (request.getFirstName() + "." + request.getLastName()).toLowerCase().replace(" ", "");
        
        // Handle duplicate usernames if necessary (simplified here)
        int count = 1;
        String originalUsername = username;
        while (userRepository.existsByUsername(username)) {
            username = originalUsername + count++;
        }

        // 2. Create User (Parent)
        User parent = User.builder()
                .username(username)
                .password(encoder.encode(request.getDni())) // Password is the DNI
                .dni(request.getDni())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Set<Role> roles = new HashSet<>();
        Role parentRole = roleRepository.findByName(ERole.ROLE_PARENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(parentRole);
        parent.setRoles(roles);

        // 3. Add Students
        Set<Student> students = request.getChildren().stream().map(studentRequest -> {
            return Student.builder()
                    .firstName(studentRequest.getFirstName())
                    .lastName(studentRequest.getLastName())
                    .dni(studentRequest.getDni())
                    .section(studentRequest.getSection())
                    .grade(studentRequest.getGrade())
                    .level(studentRequest.getLevel())
                    .parent(parent)
                    .build();
        }).collect(Collectors.toSet());

        parent.setChildren(students);

        User savedParent = userRepository.save(parent);
        
        // 4. Generate Fees for students
        savedParent.getChildren().forEach(student -> paymentService.generateFeesForStudent(student));

        // 5. Generate Simulated Credit Card
        Random random = new Random();
        String cardNumber = String.format("%04d %04d %04d %04d", 
            4000 + random.nextInt(1000), 
            random.nextInt(10000), 
            random.nextInt(10000), 
            random.nextInt(10000));
        
        CreditCard card = CreditCard.builder()
                .cardNumber(cardNumber)
                .cardHolderName(savedParent.getFirstName().toUpperCase() + " " + savedParent.getLastName().toUpperCase())
                .expirationDate("12/28")
                .cvv(String.format("%03d", random.nextInt(1000)))
                .balance(2000.0) // Fictitious initial balance
                .user(savedParent)
                .build();
        
        creditCardRepository.save(card);

        return new MessageResponse("Parent registered successfully! Username: " + username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentResponse> getAllParents() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == ERole.ROLE_PARENT))
                .map(user -> ParentResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .dni(user.getDni())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .childrenNames(user.getChildren().stream()
                                .map(Student::getFirstName)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> StudentResponse.builder()
                        .id(student.getId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .dni(student.getDni())
                        .section(student.getSection())
                        .grade(student.getGrade())
                        .level(student.getLevel())
                        .parentName(student.getParent().getFirstName() + " " + student.getParent().getLastName())
                        .build())
                .collect(Collectors.toList());
    }
}
