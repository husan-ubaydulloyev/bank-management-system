package uz.bank.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail
    ) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendVerificationCode(
            String email,
            String code
    ) {

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto">
                    <h2 style="color:#0E7C66">Amona Bank</h2>

                    <p>Assalomu alaykum!</p>

                    <p>Emailingizni tasdiqlash kodi:</p>

                    <div style="
                        padding:20px;
                        margin:20px 0;
                        text-align:center;
                        background:#f3f4f6;
                        border-radius:12px;
                        font-size:32px;
                        font-weight:bold;
                        letter-spacing:8px;">
                        %s
                    </div>

                    <p>Kod 10 daqiqa davomida amal qiladi.</p>

                    <p style="color:#888">
                        Amona Bank
                    </p>
                </div>
                """.formatted(code);

        CreateEmailOptions request =
                CreateEmailOptions.builder()
                        .from(fromEmail)
                        .to(email)
                        .subject("Amona Bank - Email tasdiqlash kodi")
                        .html(html)
                        .build();

        try {
            CreateEmailResponse response =
                    resend.emails().send(request);

            System.out.println(
                    "Verification email yuborildi. ID: "
                            + response.getId()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Resend email yuborishda xato: "
                            + e.getMessage(),
                    e
            );
        }
    }
}