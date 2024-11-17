# McFramework

McFramework to framework do tworzenia pluginów Paper, który upraszcza zarządzanie konfiguracją, komponentami, komendami oraz subkomendami. Dzięki niemu możesz łatwo tworzyć pluginy w sposób strukturalny, wykorzystując automatyczne wstrzykiwanie zależności oraz konfigurację za pomocą adnotacji.

---

## 📚 Spis treści
- [🛠️ Wprowadzenie do McFramework](#wprowadzenie-do-mcframework)
- [✨ Konfiguracja](#konfiguracja)
    - [🔧 Tworzenie klasy konfiguracyjnej](#tworzenie-klasy-konfiguracyjnej)
    - [🫘 Adnotacja @Bean](#adnotacja-bean)
    - [🔑 Adnotacja @ConfigProperty](#adnotacja-configproperty)
- [⚡ Rejestracja komponentów](#rejestracja-komponentów)
    - [🔍 Adnotacja @Component](#adnotacja-component)
- [🛠️ Komendy i SubKomendy](#komendy-i-subkomendy)
    - [🔗 Adnotacja @Cmd i @SubCmd](#adnotacja-cmd-i-subcmd)
- [🚀 Szybki Start](#szybki-start)

---

## 🛠️ Wprowadzenie do McFramework

Framework automatyzuje zarządzanie komponentami, konfiguracją oraz rejestrowaniem komend. Pozwala Ci skupić się na logice aplikacji, bez martwienia się o szczegóły implementacyjne.

---

## 📌 Adnotacje i ich zastosowanie

### 🫘 Bean

Adnotacja `@Bean` służy do oznaczania metod, które zwracają obiekty zarządzane przez framework. Musi być używana w klasach oznaczonych `@Configuration`. Obiekt utworzony przez metodę oznaczoną `@Bean` jest zarządzany przez framework i może być automatycznie wstrzykiwany w inne komponenty.

**Przykład:**
```java
@Configuration
public class MyConfig {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

Kiedy framework napotka `@Bean`, utworzy obiekt i doda go do swojego kontekstu, umożliwiając wstrzykiwanie zależności.

### 🏗️ Component

`@Component` oznacza klasę jako zarządzaną przez framework, umożliwiając jej automatyczne wstrzykiwanie jako zależności. `@Component` działa podobnie jak `@Command` i `@SubCommand`.

**Przykład:**
```java
@Component
public class MyComponent {

    public void doSomething() {
        System.out.println("Component działa!");
    }
}
```

### ⚙️ ConfigProperty

`@ConfigProperty` służy do wstrzykiwania wartości konfiguracyjnych do pól w klasach oznaczonych `@Component`, `@Command`, lub `@SubCommand`. Wartość musi być obecna w pliku konfiguracyjnym.

**Przykład:**
```java
@Component
public class MyComponent {

    @ConfigProperty("app.setting.value")
    private String settingValue;

    public void printSetting() {
        System.out.println("Wartość ustawienia: " + settingValue);
    }
}
```

Zastosowanie:
- `@Component`
- `@Command`
- `@SubCommand`

### 🛠️ Configuration

`@Configuration` oznacza klasę, która definiuje konfigurację i może zawierać metody oznaczone `@Bean`. Framework wie, że dana klasa jest źródłem definicji zależności.

**Przykład:**
```java
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```

### ⚔️ Komendy i Subkomendy

Framework obsługuje główne komendy (`Command`) oraz subkomendy (`SubCommand`). Komendy są oznaczane `@Cmd`, a subkomendy `@SubCmd`.

#### ***Definiowanie Komend***

Aby utworzyć komendę, należy dziedziczyć po klasie Command i użyć `@Cmd`.

**Przykład:**
```java
@Cmd(name = "mycommand", aliases = {"mc"}, description = "Opis mojej komendy")
public class MyCommand extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        // code
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // code
    }
}
```

#### ***Definiowanie Subkomend***

Subkomendy dziedziczą po SubCommand i są oznaczane `@SubCmd`.

**Przykład:**
```java
@SubCmd(parent = MyCommand.class, name = "sub", permission = "mc.sub")
public class MySubCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        // code
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // code
    }
}

```

### 🚀 Jak zacząć korzystać z McFramework

#### Dodanie `McFramework.run()`
Aby uruchomić framework, należy wywołać `McFramework.run(this);` w metodzie `onEnable()` głównej klasy pluginu.

**Przykład:**
```java
public class MyPlugin extends JavaPlugin implements ConfigurablePlugin {

    @Override
    public void onEnable() {
        McFramework.run(this);
    }

    @Override
    public List<String> getConfigurationFiles() {
        return Arrays.asList("config.yml");
    }
}
```

#### Implementacja `ConfigurablePlugin`
Interfejs `ConfigurablePlugin` wymaga zdefiniowania metody `getConfigurationFiles()`, która zwraca listę plików konfiguracyjnych.

### 🔍 Przykłady

#### ***Prosty przykład Configuration i Bean***
```java
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyService();
    }
}
```
```java
@Component
public class MyComponent {

    private final MyService myService;

    public MyComponent(MyService myService) {
        this.myService = myService;
    }

    public void useService() {
        myService.performAction();
    }
}
```
lub

```java
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MyComponent {

    private final MyService myService;

    public void useService() {
        myService.performAction();
    }
}
```

#### ***Prosta komenda z subkomendą***
```java
@Cmd(name = "greet", description = "Komenda powitalna")
public class GreetCommand extends Command {

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("Witaj!");
    }
}

@SubCmd(parent = "greet", name = "personal")
public class PersonalGreetSubCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("Witaj, " + sender.getName() + "!");
    }
}
```

### 💡 Uwagi dotyczące wstrzykiwania w konstruktor <a id="uwagi-dotyczace-wstrzykiwania-w-konstruktor"></a>

- **Automatyczne wstrzykiwanie JavaPlugin**: McFramework automatycznie wstrzykuje obiekt `JavaPlugin` jako zależność w konstruktorach, co pozwala na łatwy dostęp do funkcji i API Bukkit w komponentach, komendach, czy klasach konfiguracji.
- **Bean i Component**: Obiekty oznaczone jako `@Bean` oraz `@Component` są automatycznie tworzone przez framework i mogą mieć wstrzykiwane zależności w konstruktorach, o ile są dostępne w systemie komponentów lub utworzone jako `@Bean`.

Przykład:

```java
@Component
public class ExampleComponent {
    private final JavaPlugin plugin;
    private final MyService myService;

    public ExampleComponent(JavaPlugin plugin, MyService myService) {
        this.plugin = plugin;
        this.myService = myService;
    }
}
```
lub 
```java
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExampleComponent {
    private final JavaPlugin plugin;
    private final MyService myService;
}
```