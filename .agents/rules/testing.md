# Skill: Testing

## Current Test State

All services have only the default Spring Boot test stubs. No custom unit or integration tests exist.

### Existing Test Files

| Service | Test File | Content |
|---------|-----------|---------|
| UserService | `UserServiceApplicationTests.java` | Context loads |
| ActivityService | `ActivityServiceApplicationTests.java` | Context loads |
| AiService | `AiServiceApplicationTests.java` | Context loads |
| Eureka | `EurekaApplicationTests.java` | Context loads |

### Test Dependencies Available

| Service | Test Dependencies |
|---------|-------------------|
| UserService | `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test` |
| ActivityService | `spring-boot-starter-webmvc-test` |
| AiService | `spring-boot-starter-test` |
| Eureka | `spring-boot-starter-test` |

## Recommended Testing Patterns

### Unit Test (Service Layer)
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldSaveUser_whenEmailIsNew() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .firstName("John").lastName("Doe")
                .userName("johndoe").email("john@test.com")
                .password("secret").build();

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When
        UserResponseDto result = userService.registerUser(dto);

        // Then
        assertNotNull(result);
        assertEquals("john@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrow_whenEmailExists() {
        UserRequestDto dto = UserRequestDto.builder()
                .email("existing@test.com").build();

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
    }
}
```

### Controller Integration Test (MockMvc)
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void registerUser_shouldReturn200() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").build();

        when(userService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/user/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\","
                        + "\"userName\":\"johndoe\",\"email\":\"john@test.com\","
                        + "\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }
}
```

### Kafka Integration Test
```java
@SpringBootTest
@EmbeddedKafka(topics = "activity-events")
class ActivityMessageListenerTest {

    @Autowired
    private KafkaTemplate<String, Activity> kafkaTemplate;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Test
    void shouldGenerateRecommendation_whenActivityReceived() throws Exception {
        Activity activity = Activity.builder()
                .id("test-id")
                .userId(1L)
                .activityType(ActivityType.RUNNING)
                .build();

        kafkaTemplate.send("activity-events", activity);

        // Wait for async processing
        Thread.sleep(2000);

        Recommendation rec = recommendationRepository.findByActivityId("test-id");
        assertNotNull(rec);
        assertEquals(1L, rec.getUserId());
    }
}
```

## Running Tests

```bash
# Run all tests for a service
cd <service>
mvnw.cmd test

# Run a specific test class
cd <service>
mvnw.cmd test -Dtest=UserServiceTest

# Run with coverage (requires jacoco plugin)
cd <service>
mvnw.cmd test jacoco:report
```

## Test Naming Convention

```
methodName_should<Expected>_when<Condition>
```

Examples:
- `registerUser_shouldSaveUser_whenEmailIsNew`
- `registerUser_shouldThrow_whenEmailExists`
- `validateUser_shouldReturnTrue_whenUserExists`
- `listen_shouldGenerateRecommendation_whenActivityReceived`
