# NutriFlow Project - Initial Access Credentials

Bu fayl sistem ilk dəfə `DataInitializer` tərəfindən run olunanda yaranan default istifadəçilərin giriş məlumatlarını saxlayır.

## 🔑 Giriş Məlumatları

| Rol         | Email                   | Şifrə (Plain Text) | Qeyd                          |
| :---        | :---                    | :---               |:------------------------------|
| **Admin** | `admin@nutriflow.com`   | `admin123`         | Bütün sistemə nəzarət edir    |
| **Dietitian**| `diet@nutriflow.com`    | `diet123`          | Menyu hazırlayan mütəxəssis   |
| **Caterer** | `caterer@nutriflow.com` | `caterer123`       | Yemək hazırlayan şirkət       |

---

## 🛠 Texniki Qeydlər
* **Şifrələmə:** Bazada bu şifrələr `BCrypt` alqoritmi ilə hash-lanmış şəkildə saxlanılır.
* **Təhlükəsizlik:** Layihə canlıya (production) çıxmazdan əvvəl bu fayl silinməli və ya `.gitignore` faylına əlavə edilərək serverə göndərilməməlidir.
* **Dəyişdirilmə:** `DataInitializer.java` faylındakı `passwordEncoder.encode()` hissəsini dəyişərək bu şifrələri yeniləyə bilərsiniz.