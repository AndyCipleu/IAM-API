package com.andy.iamapi.application.service;

import com.andy.iamapi.domain.exception.InvalidPasswordException;
import com.andy.iamapi.domain.exception.RoleNotFoundException;
import com.andy.iamapi.domain.exception.UserAlreadyExistsException;
import com.andy.iamapi.domain.model.Role;
import com.andy.iamapi.domain.model.User;
import com.andy.iamapi.domain.port.input.RegisterUserUseCase;
import com.andy.iamapi.domain.port.output.AuditLogger;
import com.andy.iamapi.domain.port.output.PasswordEncoder;
import com.andy.iamapi.domain.port.output.RoleRepository;
import com.andy.iamapi.domain.port.output.UserRepository;
import com.andy.iamapi.domain.util.PasswordValidator;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso de registro de usuarios.
 *
 * Esta clase orquesta las operaciones necesarias para registrar un nuevo usuario:
 * validación, hashing de contraseña, asignación de roles y persistencia.
 *
 * Principios SOLID aplicados:
 *
 *   Single Responsibility: Solo se encarga del registro de usuarios
 *   Open/Closed: Extensible via nuevos validadores sin modificar esta clase
 *   Liskov Substitution: Implementa RegisterUserUseCase, puede ser sustituida
 *   Interface Segregation:Depende solo de interfaces específicas que necesita
 *   Dependency Inversion: Depende de abstracciones (ports), no de implementaciones
 *
 *
 * @see RegisterUserUseCase
 * @see User
 */
@Service
public class RegisterUserService implements RegisterUserUseCase {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    //Dependencies (inyectadas via constructor)
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    /**
     * Constructor con inyección de dependencias.
     *
     * ¿Por qué constructor?
     *
     *   Inmutabilidad: Las dependencias son final
     *   Testeable: Fácil inyectar mocks en tests
     *   Explícito: Se ve claramente qué necesita la clase
     *   Spring-friendly: @Autowired implícito en constructor único
     *
     *
     * @param userRepository Repositorio para persistir usuarios
     * @param roleRepository Repositorio para obtener roles
     * @param passwordEncoder Encoder para hashear contraseñas
     * @param auditLogger Logger para auditoría de acciones
     */
    public RegisterUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Flujo de ejecución:
     *
     *   Verifica que el email no esté en uso
     *   Valida la fortaleza de la contraseña
     *   Hashea la contraseña con BCrypt (vía port)
     *   Crea la entidad User con factory method
     *   Asigna el rol por defecto ROLE_USER
     *   Persiste el usuario en la base de datos
     *   Registra la acción en auditoría
     *
     *
     * @param command Comando con los datos del nuevo usuario (email, password, nombres)
     * @return Usuario creado y persistido con ID generado
     * @throws UserAlreadyExistsException si el email ya está registrado
     * @throws InvalidPasswordException si la contraseña no cumple los requisitos
     * @throws RoleNotFoundException si el rol por defecto no existe en la BD
     */
    @Override
    public User execute(RegisterUserCommand command) {
        //Verificar existencia email
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        //Validar fortaleza password
        PasswordValidator.validate(command.password());

        //Hashear la password para seguridad
        String hashedPassword = passwordEncoder.encode(command.password());

        //Crear entidad de dominio
        User user = User.create(
                command.email(),
                hashedPassword,
                command.firstName(),
                command.lastName()
        );

        //Asignar rol por defecto
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RoleNotFoundException(DEFAULT_ROLE));

        user.addRole(defaultRole);

        //Persistir el usuario
        User savedUser = userRepository.save(user);

        //Auditoría
        auditLogger.logAction(
                savedUser.getId(),
                "USER_REGISTERED",
                "USER:"+ savedUser.getEmail(),
                "SYSTEM" //Ip o source del registro
        );

        return savedUser;
    }
}
