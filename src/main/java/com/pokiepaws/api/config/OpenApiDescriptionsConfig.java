package com.pokiepaws.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDescriptionsConfig {

  @Bean
  public OpenApiCustomizer endpointDescriptionsCustomizer() {
    return openApi -> {
      describe(
          openApi,
          "/api/auth/register",
          PathItem.HttpMethod.POST,
          "Rejestracja użytkownika",
          desc(
              "Tworzy nowe konto użytkownika na podstawie danych rejestracyjnych.",
              "Po poprawnej rejestracji system przygotowuje proces potwierdzenia adresu e-mail, dzięki czemu konto może zostać aktywowane przed pierwszym logowaniem.",
              "",
              "**Użycie:** formularz rejestracji właściciela lub użytkownika aplikacji.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/login",
          PathItem.HttpMethod.POST,
          "Logowanie użytkownika",
          desc(
              "Uwierzytelnia użytkownika przy pomocy adresu e-mail i hasła.",
              "Jeśli konto nie wymaga dodatkowej weryfikacji, endpoint zwraca token dostępu oraz refresh token. Jeśli MFA jest wymagane, odpowiedź informuje klienta, że należy przejść do drugiego kroku logowania.",
              "",
              "**Użycie:** główny ekran logowania w aplikacji.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/refresh",
          PathItem.HttpMethod.POST,
          "Odświeżenie tokenu",
          desc(
              "Wydaje nowy token dostępu na podstawie ważnego refresh tokenu.",
              "Pozwala aplikacji utrzymać sesję użytkownika bez ponownego podawania hasła. Niepoprawny, wygasły albo wylogowany refresh token powinien skutkować błędem autoryzacji.",
              "",
              "**Użycie:** automatyczne odświeżanie sesji po wygaśnięciu krótkiego tokenu JWT.",
              "**Dostęp:** publiczny, ale wymaga poprawnego refresh tokenu w treści żądania."));
      describe(
          openApi,
          "/api/auth/logout",
          PathItem.HttpMethod.POST,
          "Wylogowanie użytkownika",
          desc(
              "Unieważnia refresh token przekazany w treści żądania.",
              "Po wykonaniu tej operacji klient nie powinien już używać wskazanego refresh tokenu do uzyskiwania nowych tokenów dostępu.",
              "",
              "**Użycie:** ręczne wylogowanie użytkownika z aplikacji.",
              "**Dostęp:** publiczny, ale działa na refresh tokenie przekazanym w żądaniu."));
      describe(
          openApi,
          "/api/auth/2fa/verify",
          PathItem.HttpMethod.POST,
          "Weryfikacja MFA",
          desc(
              "Kończy logowanie użytkownika, który po poprawnym haśle musi podać kod drugiego składnika.",
              "Endpoint sprawdza token MFA i po poprawnej weryfikacji zwraca standardową odpowiedź logowania z tokenami sesji.",
              "",
              "**Użycie:** drugi krok logowania po odpowiedzi `mfaRequired=true` z endpointu logowania.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/verify-email",
          PathItem.HttpMethod.GET,
          "Weryfikacja e-maila",
          desc(
              "Potwierdza adres e-mail użytkownika na podstawie tokenu weryfikacyjnego przekazanego w parametrze query.",
              "Po poprawnej weryfikacji konto może zostać użyte do logowania zgodnie z regułami systemu.",
              "",
              "**Użycie:** link aktywacyjny wysyłany w wiadomości e-mail.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/forgot-password",
          PathItem.HttpMethod.POST,
          "Żądanie resetu hasła",
          desc(
              "Rozpoczyna proces resetowania hasła dla konta powiązanego z podanym adresem e-mail.",
              "System wysyła wiadomość z linkiem resetującym, jeśli konto może przejść przez procedurę resetu.",
              "",
              "**Użycie:** formularz „Nie pamiętasz hasła?”.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/reset-password",
          PathItem.HttpMethod.GET,
          "Przekierowanie do resetu hasła",
          desc(
              "Sprawdza token resetu hasła i przekierowuje użytkownika do odpowiedniego widoku aplikacji frontendowej.",
              "Poprawny token trafia do ekranu ustawiania nowego hasła, a niepoprawny token do widoku błędu.",
              "",
              "**Użycie:** link klikany z wiadomości e-mail resetującej hasło.",
              "**Dostęp:** publiczny, bez tokenu JWT."));
      describe(
          openApi,
          "/api/auth/reset-password",
          PathItem.HttpMethod.POST,
          "Reset hasła",
          desc(
              "Ustawia nowe hasło użytkownika na podstawie poprawnego tokenu resetującego.",
              "Endpoint powinien być wywoływany po otwarciu formularza resetu hasła i podaniu nowego hasła przez użytkownika.",
              "",
              "**Użycie:** finalny krok procesu resetowania hasła.",
              "**Dostęp:** publiczny, bez tokenu JWT."));

      describe(
          openApi,
          "/api/clinics",
          PathItem.HttpMethod.GET,
          "Lista klinik",
          desc(
              "Zwraca listę klinik zarejestrowanych w systemie.",
              "Dane mogą być używane w aplikacji mobilnej lub panelu administracyjnym do prezentowania placówek, ich adresów, godzin pracy i danych kontaktowych.",
              "",
              "**Dostęp:** publiczny."));
      describe(
          openApi,
          "/api/clinics/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły kliniki",
          desc(
              "Zwraca pełne dane jednej kliniki wskazanej identyfikatorem z adresu URL.",
              "Endpoint jest przydatny przy otwieraniu szczegółów placówki, wybieraniu miejsca wizyty lub edycji danych kliniki.",
              "",
              "**Dostęp:** publiczny."));
      describe(
          openApi,
          "/api/clinics/city/{city}",
          PathItem.HttpMethod.GET,
          "Kliniki w mieście",
          desc(
              "Filtruje kliniki po nazwie miasta przekazanej w ścieżce.",
              "Pozwala klientowi zawęzić listę placówek do lokalizacji wybranej przez użytkownika.",
              "",
              "**Dostęp:** publiczny."));
      describe(
          openApi,
          "/api/clinics",
          PathItem.HttpMethod.POST,
          "Utworzenie kliniki",
          desc(
              "Tworzy nową klinikę wraz z danymi identyfikacyjnymi, adresowymi, kontaktowymi i godzinami pracy.",
              "Operacja służy do administracyjnego dodawania placówek do sieci PokiePaws.",
              "",
              "**Dostęp:** tylko administrator."));
      describe(
          openApi,
          "/api/clinics/{id}",
          PathItem.HttpMethod.PUT,
          "Aktualizacja kliniki",
          desc(
              "Zastępuje dane kliniki o podanym identyfikatorze wartościami z żądania.",
              "Używane do korekty danych adresowych, kontaktowych, statusu aktywności lub godzin pracy placówki.",
              "",
              "**Dostęp:** tylko administrator."));
      describe(
          openApi,
          "/api/clinics/{id}",
          PathItem.HttpMethod.DELETE,
          "Usunięcie kliniki",
          desc(
              "Usuwa klinikę wskazaną identyfikatorem z adresu URL.",
              "Przed użyciem należy upewnić się, że usunięcie placówki nie naruszy powiązań biznesowych, takich jak lekarze, wizyty lub stany magazynowe.",
              "",
              "**Dostęp:** tylko administrator."));

      describe(
          openApi,
          "/api/vets",
          PathItem.HttpMethod.GET,
          "Lista weterynarzy",
          desc(
              "Zwraca listę profili weterynarzy dostępnych w systemie.",
              "Odpowiedź może być używana do panelu administracyjnego, wyboru lekarza podczas rezerwowania wizyty oraz prezentowania personelu klinik.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony zgodnie z regułami bezpieczeństwa endpointu."));
      describe(
          openApi,
          "/api/vets/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły weterynarza",
          desc(
              "Zwraca dane pojedynczego weterynarza wskazanego identyfikatorem.",
              "Endpoint służy do podglądu lub edycji profilu lekarza, w tym danych powiązanych z kliniką.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony."));
      describe(
          openApi,
          "/api/vets/clinic/{clinicId}",
          PathItem.HttpMethod.GET,
          "Weterynarze kliniki",
          desc(
              "Zwraca weterynarzy przypisanych do wskazanej kliniki.",
              "Najczęściej używane przy wyborze lekarza po wcześniejszym wyborze placówki lub przy zarządzaniu personelem konkretnej kliniki.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony."));
      describe(
          openApi,
          "/api/vets",
          PathItem.HttpMethod.POST,
          "Utworzenie weterynarza",
          desc(
              "Tworzy profil weterynarza oraz powiązane dane potrzebne do obsługi wizyt.",
              "Żądanie powinno zawierać komplet informacji wymaganych przez formularz tworzenia lekarza.",
              "",
              "**Dostęp:** użytkownik z odpowiednim uprawnieniem administracyjnym."));
      describe(
          openApi,
          "/api/vets/{id}",
          PathItem.HttpMethod.PUT,
          "Aktualizacja weterynarza",
          desc(
              "Aktualizuje dane weterynarza o podanym identyfikatorze.",
              "Operacja służy do zmiany danych osobowych, przypisania do kliniki lub innych informacji używanych w procesie rezerwacji wizyt.",
              "",
              "**Dostęp:** użytkownik z odpowiednim uprawnieniem administracyjnym."));
      describe(
          openApi,
          "/api/vets/{id}",
          PathItem.HttpMethod.DELETE,
          "Usunięcie weterynarza",
          desc(
              "Usuwa profil weterynarza wskazany identyfikatorem.",
              "Przed wykonaniem operacji należy uwzględnić możliwe powiązania z wizytami i dostępnością lekarza.",
              "",
              "**Dostęp:** użytkownik z odpowiednim uprawnieniem administracyjnym."));

      describe(
          openApi,
          "/api/animals",
          PathItem.HttpMethod.GET,
          "Lista zwierząt",
          desc(
              "Zwraca listę zwierząt dostępnych dla aktualnego kontekstu użytkownika.",
              "Endpoint jest przeznaczony do przeglądania profili zwierząt i może być wykorzystywany w panelach obsługi oraz widokach właściciela.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony."));
      describe(
          openApi,
          "/api/animals/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły zwierzęcia",
          desc(
              "Zwraca szczegółowe dane zwierzęcia wskazanego identyfikatorem.",
              "Odpowiedź może zawierać informacje potrzebne do wyświetlenia profilu, planowania wizyty lub aktualizacji danych zwierzęcia.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony z dostępem do danego rekordu."));
      describe(
          openApi,
          "/api/animals/owner/{ownerId}",
          PathItem.HttpMethod.GET,
          "Zwierzęta właściciela",
          desc(
              "Zwraca zwierzęta przypisane do właściciela wskazanego identyfikatorem.",
              "Endpoint ułatwia pracę administracji lub personelu, gdy trzeba sprawdzić zwierzęta konkretnego klienta.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony z odpowiednimi uprawnieniami."));
      describe(
          openApi,
          "/api/animals",
          PathItem.HttpMethod.POST,
          "Dodanie zwierzęcia",
          desc(
              "Tworzy nowy profil zwierzęcia i przypisuje go zgodnie z danymi przekazanymi w żądaniu.",
              "Wymagane dane powinny pozwalać systemowi jednoznacznie powiązać zwierzę z właścicielem oraz późniejszymi wizytami.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony."));
      describe(
          openApi,
          "/api/animals/{id}",
          PathItem.HttpMethod.PUT,
          "Aktualizacja zwierzęcia",
          desc(
              "Aktualizuje profil zwierzęcia wskazanego identyfikatorem.",
              "Operacja służy do zmiany danych takich jak imię, gatunek, rasa, data urodzenia lub inne informacje opisujące zwierzę.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony z dostępem do danego zwierzęcia."));
      describe(
          openApi,
          "/api/animals/{id}",
          PathItem.HttpMethod.DELETE,
          "Usunięcie zwierzęcia",
          desc(
              "Usuwa profil zwierzęcia wskazany identyfikatorem.",
              "Przed użyciem należy wziąć pod uwagę powiązane wizyty i historię medyczną.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony z odpowiednimi uprawnieniami."));

      describe(
          openApi,
          "/api/visits",
          PathItem.HttpMethod.POST,
          "Utworzenie wizyty",
          desc(
              "Rezerwuje nową wizytę dla zwierzęcia u wybranego weterynarza w określonej klinice i terminie.",
              "System powinien zweryfikować dostępność terminu, właściciela zwierzęcia oraz poprawność danych rezerwacji.",
              "",
              "**Dostęp:** właściciel zwierzęcia."));
      describe(
          openApi,
          "/api/visits/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły wizyty",
          desc(
              "Zwraca szczegóły wizyty wskazanej identyfikatorem.",
              "Właściciel może pobrać tylko wizytę należącą do jego zwierzęcia, zgodnie z walidacją po stronie serwisu.",
              "",
              "**Dostęp:** właściciel powiązany z wizytą."));
      describe(
          openApi,
          "/api/visits/{id}/cancel",
          PathItem.HttpMethod.PATCH,
          "Anulowanie wizyty",
          desc(
              "Anuluje zaplanowaną wizytę właściciela.",
              "Operacja zmienia status wizyty zamiast tworzyć nowy rekord. Może być ograniczona regułami biznesowymi dotyczącymi statusu lub czasu do rozpoczęcia wizyty.",
              "",
              "**Dostęp:** właściciel powiązany z wizytą."));
      describe(
          openApi,
          "/api/animals/{animalId}/visits",
          PathItem.HttpMethod.GET,
          "Wizyty zwierzęcia",
          desc(
              "Zwraca historię lub listę wizyt konkretnego zwierzęcia.",
              "Serwis sprawdza, czy wskazane zwierzę należy do aktualnie zalogowanego właściciela.",
              "",
              "**Dostęp:** właściciel danego zwierzęcia."));
      describe(
          openApi,
          "/api/owners/me/visits",
          PathItem.HttpMethod.GET,
          "Wizyty zalogowanego właściciela",
          desc(
              "Zwraca wizyty aktualnie zalogowanego właściciela z podanego zakresu dat.",
              "Parametry `from` i `to` powinny być przekazane jako daty w formacie ISO, na przykład `2026-05-01`.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/visits/upcoming",
          PathItem.HttpMethod.GET,
          "Nadchodzące wizyty właściciela",
          desc(
              "Zwraca najbliższe wizyty zaplanowane dla zwierząt aktualnie zalogowanego właściciela.",
              "Endpoint jest przeznaczony do ekranów startowych i list przypomnień o nadchodzących terminach.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/vets/me/visits",
          PathItem.HttpMethod.GET,
          "Wizyty zalogowanego weterynarza",
          desc(
              "Zwraca wizyty przypisane do aktualnie zalogowanego weterynarza.",
              "Lista może być filtrowana parametrami obsługiwanymi przez kontroler i służy do pracy z harmonogramem lekarza.",
              "",
              "**Dostęp:** weterynarz."));
      describe(
          openApi,
          "/api/vets/me/visits/upcoming",
          PathItem.HttpMethod.GET,
          "Nadchodzące wizyty weterynarza",
          desc(
              "Zwraca najbliższe wizyty przypisane do aktualnie zalogowanego weterynarza.",
              "Endpoint wspiera widoki terminarza i szybkie przygotowanie lekarza do nadchodzących konsultacji.",
              "",
              "**Dostęp:** weterynarz."));
      describe(
          openApi,
          "/api/vets/me/visits/{id}/medical-data",
          PathItem.HttpMethod.PATCH,
          "Aktualizacja danych medycznych wizyty",
          desc(
              "Zapisuje dane medyczne uzupełniane przez weterynarza w ramach konkretnej wizyty.",
              "Żądanie może obejmować rozpoznanie, zalecenia, notatki lub inne pola medyczne przewidziane w DTO wizyty.",
              "",
              "**Dostęp:** weterynarz przypisany do wizyty."));
      describe(
          openApi,
          "/api/vets/me/visits/{id}/confirm",
          PathItem.HttpMethod.POST,
          "Potwierdzenie wizyty",
          desc(
              "Potwierdza realizację wizyty przez aktualnie zalogowanego weterynarza.",
              "Operacja jest używana po zakończeniu konsultacji i może wywoływać dalsze działania, takie jak powiadomienie właściciela.",
              "",
              "**Dostęp:** weterynarz przypisany do wizyty."));
      describe(
          openApi,
          "/api/clinics/{clinicId}/vets/{vetUserId}/available-slots",
          PathItem.HttpMethod.GET,
          "Dostępne terminy",
          desc(
              "Zwraca wolne sloty wizyt dla wybranego weterynarza w konkretnej klinice.",
              "Endpoint pomaga klientowi zbudować kalendarz rezerwacji i powinien być wywoływany po wyborze placówki oraz lekarza.",
              "",
              "**Dostęp:** użytkownik uwierzytelniony lub publiczny zgodnie z konfiguracją kontrolera."));

      describe(
          openApi,
          "/api/visits/{id}/prescription",
          PathItem.HttpMethod.POST,
          "Utworzenie recepty",
          desc(
              "Tworzy receptę dla wskazanej wizyty.",
              "Żądanie powinno zawierać pozycje recepty, dawki i zalecenia zgodne z modelem recept w systemie.",
              "",
              "**Dostęp:** weterynarz obsługujący wizytę."));
      describe(
          openApi,
          "/api/visits/{id}/prescription",
          PathItem.HttpMethod.GET,
          "Recepta wizyty",
          desc(
              "Zwraca receptę przypisaną do wskazanej wizyty.",
              "Endpoint służy do podglądu zaleceń i leków po zakończeniu konsultacji.",
              "",
              "**Dostęp:** użytkownik z dostępem do wizyty."));

      describe(
          openApi,
          "/api/owners/me",
          PathItem.HttpMethod.GET,
          "Profil właściciela",
          desc(
              "Zwraca dane profilu aktualnie zalogowanego właściciela.",
              "Odpowiedź jest przeznaczona do ekranu ustawień konta i może zawierać dane kontaktowe oraz adresowe.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/phone",
          PathItem.HttpMethod.PATCH,
          "Zmiana telefonu właściciela",
          desc(
              "Aktualizuje numer telefonu aktualnie zalogowanego właściciela.",
              "Endpoint zwraca pustą odpowiedź po poprawnym zapisaniu zmiany.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/address",
          PathItem.HttpMethod.PATCH,
          "Zmiana adresu właściciela",
          desc(
              "Aktualizuje dane adresowe aktualnie zalogowanego właściciela.",
              "Operacja jest przeznaczona do formularza ustawień profilu i zapisuje tylko pola adresu przekazane w żądaniu.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/password",
          PathItem.HttpMethod.PATCH,
          "Zmiana hasła właściciela",
          desc(
              "Zmienia hasło aktualnie zalogowanego właściciela.",
              "Żądanie powinno zawierać dane wymagane do walidacji starego hasła i zapisania nowego hasła zgodnie z polityką bezpieczeństwa.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me",
          PathItem.HttpMethod.DELETE,
          "Usunięcie konta właściciela",
          desc(
              "Usuwa konto aktualnie zalogowanego właściciela.",
              "Operacja jest nieodwracalna z perspektywy aplikacji klienckiej i powinna być poprzedzona potwierdzeniem w interfejsie użytkownika.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/device-tokens",
          PathItem.HttpMethod.POST,
          "Rejestracja tokenu urządzenia",
          desc(
              "Zapisuje token urządzenia aktualnie zalogowanego właściciela do obsługi powiadomień push.",
              "Endpoint powinien być wywoływany po uzyskaniu lub odświeżeniu tokenu z usługi powiadomień na urządzeniu mobilnym.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/device-tokens",
          PathItem.HttpMethod.GET,
          "Tokeny urządzeń właściciela",
          desc(
              "Zwraca tokeny urządzeń powiązane z aktualnie zalogowanym właścicielem.",
              "Endpoint jest pomocny diagnostycznie oraz przy zarządzaniu wieloma urządzeniami użytkownika.",
              "",
              "**Dostęp:** właściciel."));
      describe(
          openApi,
          "/api/owners/me/device-tokens",
          PathItem.HttpMethod.DELETE,
          "Usunięcie tokenu urządzenia",
          desc(
              "Usuwa wskazany token urządzenia aktualnie zalogowanego właściciela.",
              "Operacja jest używana przy wylogowaniu z urządzenia, wyłączeniu powiadomień albo wycofaniu nieaktualnego tokenu.",
              "",
              "**Dostęp:** właściciel."));

      describe(
          openApi,
          "/api/admin/users",
          PathItem.HttpMethod.GET,
          "Lista użytkowników",
          desc(
              "Zwraca listę użytkowników systemu dla panelu administracyjnego.",
              "Pozwala administratorowi przeglądać konta, role i podstawowe dane identyfikujące użytkowników.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/users/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły użytkownika",
          desc(
              "Zwraca szczegóły użytkownika wskazanego identyfikatorem.",
              "Endpoint jest używany przy podglądzie lub edycji konta w panelu administracyjnym.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/users",
          PathItem.HttpMethod.POST,
          "Utworzenie użytkownika",
          desc(
              "Tworzy nowe konto użytkownika z poziomu panelu administracyjnego.",
              "Żądanie powinno określać dane konta oraz rolę, która zdecyduje o dostępie do funkcji systemu.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/users/{id}",
          PathItem.HttpMethod.PUT,
          "Aktualizacja użytkownika",
          desc(
              "Aktualizuje dane użytkownika wskazanego identyfikatorem.",
              "Operacja może zmieniać dane profilu oraz konfigurację konta zgodnie z zakresem DTO administracyjnego.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/users/{id}",
          PathItem.HttpMethod.DELETE,
          "Usunięcie użytkownika",
          desc(
              "Usuwa konto użytkownika wskazane identyfikatorem.",
              "Przed wykonaniem operacji należy sprawdzić wpływ usunięcia na powiązane rekordy, takie jak właściciel, weterynarz albo historia aktywności.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/logs",
          PathItem.HttpMethod.GET,
          "Logi aktywności",
          desc(
              "Zwraca logi aktywności użytkowników w systemie.",
              "Endpoint wspiera audyt działań, diagnostykę i analizę zachowania użytkowników w panelu administracyjnym.",
              "",
              "**Dostęp:** administrator."));
      describe(
          openApi,
          "/api/admin/logs/stats",
          PathItem.HttpMethod.GET,
          "Statystyki logów aktywności",
          desc(
              "Zwraca zagregowane statystyki na podstawie logów aktywności.",
              "Dane mogą być używane do dashboardów administracyjnych i szybkiego monitorowania aktywności w systemie.",
              "",
              "**Dostęp:** administrator."));

      describe(
          openApi,
          "/api/orders",
          PathItem.HttpMethod.GET,
          "Lista zamówień klinik",
          desc(
              "Zwraca zamówienia asortymentu składane przez kliniki.",
              "Endpoint służy do obsługi przepływu zaopatrzenia między klinikami a magazynem.",
              "",
              "**Dostęp:** użytkownik z uprawnieniami do obsługi zamówień."));
      describe(
          openApi,
          "/api/orders",
          PathItem.HttpMethod.POST,
          "Utworzenie zamówienia kliniki",
          desc(
              "Tworzy nowe zamówienie asortymentu dla kliniki.",
              "Żądanie powinno zawierać pozycje zamówienia oraz dane pozwalające powiązać je z właściwą placówką.",
              "",
              "**Dostęp:** użytkownik z uprawnieniami do składania zamówień."));
      describe(
          openApi,
          "/api/orders/{id}/status",
          PathItem.HttpMethod.PUT,
          "Zmiana statusu zamówienia",
          desc(
              "Aktualizuje status zamówienia asortymentu wskazanego identyfikatorem.",
              "Operacja wspiera proces realizacji zamówień, na przykład przejście między etapami oczekiwania, realizacji i zakończenia.",
              "",
              "**Dostęp:** użytkownik z uprawnieniami do obsługi zamówień."));

      describe(
          openApi,
          "/api/warehouse-workers/me",
          PathItem.HttpMethod.GET,
          "Profil pracownika magazynu",
          desc(
              "Zwraca profil aktualnie zalogowanego pracownika magazynu.",
              "Endpoint pozwala frontendowi ustalić kontekst pracownika i jego powiązanie z magazynem.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock",
          PathItem.HttpMethod.GET,
          "Lista stanów magazynowych",
          desc(
              "Zwraca wszystkie pozycje stanów magazynowych dostępne dla pracownika magazynu.",
              "Odpowiedź zawiera dane potrzebne do przeglądania zapasów, produktów, ilości i progów magazynowych.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock/warehouse/{warehouseId}",
          PathItem.HttpMethod.GET,
          "Stany magazynowe wybranego magazynu",
          desc(
              "Zwraca pozycje magazynowe przypisane do wskazanego magazynu.",
              "Endpoint jest używany przy pracy z konkretną lokalizacją magazynową i pozwala ograniczyć listę zapasów do jednego magazynu.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock/low-stock",
          PathItem.HttpMethod.GET,
          "Niskie stany magazynowe",
          desc(
              "Zwraca pozycje, których ilość jest równa lub niższa od wskazanego progu.",
              "Parametr `threshold` jest opcjonalny i domyślnie wynosi `10`. Endpoint pomaga szybko wykrywać produkty wymagające uzupełnienia.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock/{id}",
          PathItem.HttpMethod.GET,
          "Szczegóły pozycji magazynowej",
          desc(
              "Zwraca szczegóły jednej pozycji magazynowej wskazanej identyfikatorem.",
              "Odpowiedź jest używana do podglądu produktu, ilości i danych magazynu przed edycją.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock",
          PathItem.HttpMethod.POST,
          "Dodanie pozycji magazynowej",
          desc(
              "Tworzy nową pozycję stanu magazynowego.",
              "Żądanie powinno wskazywać produkt, magazyn oraz ilość lub inne wymagane dane opisane w DTO.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock/{id}",
          PathItem.HttpMethod.PUT,
          "Aktualizacja pozycji magazynowej",
          desc(
              "Aktualizuje pozycję magazynową wskazaną identyfikatorem.",
              "Operacja służy do korekty ilości, przypisania lub innych danych pozycji magazynowej.",
              "",
              "**Dostęp:** pracownik magazynu."));
      describe(
          openApi,
          "/api/warehouse/stock/{id}",
          PathItem.HttpMethod.DELETE,
          "Usunięcie pozycji magazynowej",
          desc(
              "Usuwa pozycję magazynową wskazaną identyfikatorem.",
              "Operacja powinna być używana ostrożnie, ponieważ usuwa rekord stanu magazynowego z systemu.",
              "",
              "**Dostęp:** pracownik magazynu."));
    };
  }

  private String desc(String... lines) {
    return String.join("\n", lines);
  }

  private void describe(
      OpenAPI openApi,
      String path,
      PathItem.HttpMethod method,
      String summary,
      String description) {
    PathItem pathItem = openApi.getPaths().get(path);
    if (pathItem == null) {
      return;
    }

    Operation operation = pathItem.readOperationsMap().get(method);
    if (operation == null) {
      return;
    }

    operation.setSummary(summary);
    operation.setDescription(description);
  }
}
