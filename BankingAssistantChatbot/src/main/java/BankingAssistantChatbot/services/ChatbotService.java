package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class ChatbotService {

    private final AiClientService aiClientService;
    private final BankingService bankingService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    private enum Lang { TR, EN }

    private static final Pattern TR_IBAN_ANYWHERE = Pattern.compile("\\bTR\\d{24}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern AMOUNT_ANYWHERE  = Pattern.compile("(?<!\\d)(\\d+(?:[\\.,]\\d+)?)\\b");

    private static final Set<String> TR_HINTS = Set.of(
            "bakiye","bakiyem","gönder","gonder","havale","eft","işlem","islem","son","yardım","yardim","nasıl","nasil"
    );
    private static final Set<String> EN_HINTS = Set.of(
            "balance","send","transfer","how","last","transactions","help","format"
    );

    public ChatbotService(AiClientService aiClientService,
                          BankingService bankingService,
                          TransactionService transactionService,
                          AccountRepository accountRepository) {
        this.aiClientService = aiClientService;
        this.bankingService = bankingService;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
    }

    public String generateResponse(String userIban, String userMessage) {
        if (userIban == null || userIban.isBlank()) {
            return trOrEn(detectLanguage(userMessage),
                    "Lütfen IBAN bilgisini girin.",
                    "Please provide your IBAN.");
        }

        Lang lang = detectLanguage(userMessage);

        Account account = accountRepository.findByIban(userIban).orElse(null);
        if (account == null) {
            return trOrEn(lang, "Hesap bulunamadı.", "Account not found.");
        }

        if (!isUserAuthenticated(userIban)) {
            return trOrEn(lang, "Önce giriş yapmalısınız.", "You need to log in first.");
        }

        if (userMessage == null || userMessage.isBlank()) {
            return trOrEn(lang, "Lütfen bir mesaj yazın.", "Please type a message.");
        }

        String raw = userMessage.trim();
        String msg = normalize(raw);

        if (containsAny(msg, "iban format", "iban nasıl", "iban nasil", "iban formatı", "iban formati")) {
            return trOrEn(lang,
                    "IBAN, uluslararası banka hesap numarasıdır. Türkiye için format: TR + 2 kontrol hanesi + 24 rakam (toplam 26 karakter). Örn: TR12000620000000000000000001",
                    "IBAN is an International Bank Account Number. For Turkey: 'TR' + 24 digits (26 chars total). Example: TR12000620000000000000000001");
        }

        if (containsAny(msg, "transfer nasıl yapılır", "transfer nasil yapilir", "how to transfer", "how do i transfer")) {
            return trOrEn(lang,
                    "Transfer yapmak için alıcı IBAN ve tutarı belirtin. Örn: 'TRxxxxxxxxxxxxxxxxxxxxxxxx 100 TL gönder'.",
                    "To make a transfer, provide recipient IBAN and amount. Example: 'Send 100 TL to TRxxxxxxxxxxxxxxxxxxxxxxxx'.");
        }

        if (containsAny(msg, "bakiye", "bakiyem", "balance")) {
            Double balance = bankingService.getBalance(userIban);
            if (balance == null) {
                return trOrEn(lang, "IBAN'ınıza ait hesap bulunamadı.", "I could not find an account for your IBAN.");
            }
            return trOrEn(lang,
                    "Güncel bakiyeniz: " + formatMoney(balance) + " TL.",
                    "Your current balance is: " + formatMoney(balance) + " TL.");
        }

        if (containsAny(msg, "son 10 işlem", "son 10 islem", "last 10 transactions")) {
            List<Transaction> tx = transactionService.getLast10Transactions(account);
            if (tx == null || tx.isEmpty()) {
                return trOrEn(lang, "Yakın zamanda işlem bulunamadı.", "No recent transactions found.");
            }
            return trOrEn(lang,
                    "Son 10 işleminiz:\n" + formatTransactions(tx),
                    "Your last 10 transactions:\n" + formatTransactions(tx));
        }

        if (containsAny(msg, "transfer", "gönder", "gonder", "send")) {
            String targetIban = extractIban(raw);
            Double amount = extractAmount(raw);

            if (amount == null || amount <= 0) {
                return trOrEn(lang,
                        "Gönderilecek tutarı yazın. Örn: '100 TL gönder'.",
                        "Please specify the amount. Example: 'Send 100 TL'.");
            }

            if (targetIban == null) {
                return trOrEn(lang,
                        "Alıcı IBAN'ı yazın. Örn: 'TR... 100 TL gönder'.",
                        "Please provide the recipient IBAN. Example: 'TR... send 100 TL'.");
            }

            if (!isValidTrIban(targetIban)) {
                return trOrEn(lang,
                        "IBAN formatı hatalı. TR ile başlayan 26 karakterlik IBAN girin.",
                        "Invalid IBAN format. Provide a Turkish IBAN starting with TR (26 chars).");
            }

            Double balance = bankingService.getBalance(userIban);
            if (balance == null || balance < amount) {
                return trOrEn(lang,
                        "Bu transfer için bakiyeniz yetersiz.",
                        "Your balance is insufficient for this transfer.");
            }

            boolean success = bankingService.transfer(userIban, targetIban, amount);
            return success
                    ? trOrEn(lang,
                    "Transfer başarılı: " + formatMoney(amount) + " TL, " + targetIban + " IBAN'ına gönderildi.",
                    "Transfer successful: " + formatMoney(amount) + " TL sent to " + targetIban + ".")
                    : trOrEn(lang,
                    "Transfer başarısız. IBAN'ı kontrol edin veya daha sonra tekrar deneyin.",
                    "Transfer failed. Please check the IBAN or try again later.");
        }

        return aiClientService.askModel(userMessage);
    }

    private boolean isValidTrIban(String iban) {
        return iban != null && iban.matches("^TR\\d{24}$");
    }

    private boolean isUserAuthenticated(String userIban) {
        return userIban != null && !userIban.isBlank();
    }

    private Lang detectLanguage(String message) {
        if (message == null || message.isBlank()) return Lang.TR;
        if (message.matches(".*[şŞçÇıİğĞöÖüÜ].*")) return Lang.TR;

        String m = normalize(message);

        for (String h : TR_HINTS) if (m.contains(h)) return Lang.TR;
        for (String h : EN_HINTS) if (m.contains(h)) return Lang.EN;

        return Lang.TR;
    }

    private String trOrEn(Lang lang, String tr, String en) {
        return (lang == Lang.TR) ? tr : en;
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }

    private boolean containsAny(String msg, String... keys) {
        for (String k : keys) if (msg.contains(k)) return true;
        return false;
    }

    private String extractIban(String raw) {
        if (raw == null) return null;
        String compact = raw.replace(" ", "");
        Matcher m = TR_IBAN_ANYWHERE.matcher(compact);
        if (m.find()) return m.group().toUpperCase(Locale.ROOT);
        return null;
    }

    private Double extractAmount(String raw) {
        if (raw == null) return null;
        Matcher m = AMOUNT_ANYWHERE.matcher(raw);
        if (!m.find()) return null;
        String val = m.group(1).replace(",", ".");
        try { return Double.parseDouble(val); } catch (Exception e) { return null; }
    }

    private String formatMoney(Double v) {
        if (v == null) return "0";
        if (Math.abs(v - Math.round(v)) < 1e-9) return String.valueOf(Math.round(v));
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private String formatTransactions(List<Transaction> tx) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Transaction t : tx) {
            sb.append(i++).append(") ").append(String.valueOf(t)).append("\n");
        }
        return sb.toString().trim();
    }
}
