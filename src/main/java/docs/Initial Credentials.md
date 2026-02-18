# NutriFlow Project - Initial Access Credentials

This file contains the default user credentials generated when the system
is first initialized by `DataInitializer`.

## 🔑 Login Credentials

| Role          | Email                   | Password (Plain Text) | Note                              |
| :---          | :---                    | :---                  | :---                              |
| **Admin**     | `admin@nutriflow.com`   | `admin123`            | Full system access                |
| **Dietitian** | `diet@nutriflow.com`    | `diet123`             | Nutrition specialist              |
| **Caterer**   | `caterer@nutriflow.com` | `caterer123`          | Food preparation company          |

---

## 🛠 Technical Notes
* **Encryption:** Passwords are stored in the database as BCrypt hashed values.
* **Security:** This file must be deleted or added to `.gitignore` before deploying to production.
* **Modification:** Passwords can be updated by changing the `passwordEncoder.encode()` method in `DataInitializer.java`.