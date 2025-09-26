package life.pharmacy.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter[] SUPPORTED_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,                 // 2025-09-27
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),        // 27/09/2025
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),        // 09/27/2025
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),        // 27-09-2025
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),        // 2025/09/27
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),        // 27.09.2025
            DateTimeFormatter.ofPattern("dd MMM yyyy"),       // 27 Sep 2025
            DateTimeFormatter.ofPattern("dd MMMM yyyy")       // 27 Septembre 2025
    };

    /**
     * Convertit une chaîne Excel en LocalDate.
     * Gère plusieurs formats et supprime les espaces parasites.
     *
     * @param dateStr la chaîne de date (peut venir d’Excel)
     * @return LocalDate parsée ou null si la chaîne est vide
     * @throws IllegalArgumentException si aucun format ne correspond
     */
    public static LocalDate parseExcelDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        String clean = dateStr.trim();

        for (DateTimeFormatter fmt : SUPPORTED_FORMATS) {
            try {
                return LocalDate.parse(clean, fmt);
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Format de date inconnu: " + clean);
    }
}
