package com.example.npm_b01.i18n

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    VI("vi", "Tiếng Việt", "🇻🇳"),
    EN("en", "English", "🇬🇧")
}

object AppStrings {

    fun get(key: String, language: AppLanguage): String {
        val isVi = language == AppLanguage.VI
        return when (key) {
            "app_title" -> "BLOCK PUZZLE 🧩"
            "best_score" -> if (isVi) "🏆 ĐIỂM CAO NHẤT: " else "🏆 BEST HIGH SCORE: "
            "played" -> if (isVi) "ĐÃ CHƠI" else "PLAYED"
            "cleared" -> if (isVi) "ĐÃ XÓA" else "CLEARED"
            "games_count" -> if (isVi) "ván" else "games"
            "lines_count" -> if (isVi) "hàng" else "lines"

            "mode_free_title" -> if (isVi) "🎮 CHƠI TỰ DO" else "🎮 FREE PLAY"
            "mode_free_badge" -> if (isVi) "Kinh Điển" else "Classic"
            "mode_free_desc" -> if (isVi) "Bàn cờ trống hoàn toàn. Tự do xếp hình & thử thách điểm cao không giới hạn." else "Empty board. Drag & drop freely to challenge your high score."

            "mode_preset_title" -> if (isVi) "🧩 XẾP HÌNH BÀN CỜ" else "🧩 PRESET PUZZLE"
            "mode_preset_badge" -> if (isVi) "Thử Thách" else "Challenge"
            "mode_preset_desc" -> if (isVi) "Bàn cờ có sẵn một số khối gạch vật cản. Lựa chọn các khối bên dưới để giải đố." else "Board with pre-placed obstacles. Choose blocks below to solve the puzzle."

            "mode_gravity_title" -> if (isVi) "⏬ THẢ KHỐI RƠI" else "⏬ GRAVITY DROP"
            "mode_gravity_badge" -> if (isVi) "Rơi Tự Do" else "Free Fall"
            "mode_gravity_desc" -> if (isVi) "Khối gạch rơi từ trên xuống. Di chuyển và thả rơi để lấp đầy các ô bên dưới." else "Blocks fall automatically. Move & drop to fill columns below."

            "play_now" -> if (isVi) "VÀO CHƠI ➔" else "PLAY NOW ➔"

            "settings_title" -> if (isVi) "⚙️ CÀI ĐẶT" else "⚙️ SETTINGS"
            "language_setting" -> if (isVi) "🌐 Ngôn Ngữ / Language" else "🌐 Language"
            "privacy_policy" -> if (isVi) "🔒 Chính Sách Bảo Mật" else "🔒 Privacy Policy"
            "terms_of_service" -> if (isVi) "📜 Điều Khoản Sử Dụng" else "📜 Terms of Service"

            "privacy_content" -> if (isVi)
                "CHÍNH SÁCH BẢO MẬT\n\n1. Thu thập dữ liệu:\nGame lưu trữ kỷ lục điểm số và tiến trình chơi cục bộ trên thiết bị của bạn qua SharedPreferences.\n\n2. Quyền riêng tư:\nChúng tôi cam kết không thu thập, chia sẻ hay bán bất kỳ thông tin cá nhân nào của người dùng.\n\n3. An toàn dữ liệu:\nTất cả dữ liệu trò chơi được bảo mật an toàn ngay trên điện thoại của bạn."
            else
                "PRIVACY POLICY\n\n1. Data Collection:\nThe game saves your high scores and game progress locally on your device via SharedPreferences.\n\n2. User Privacy:\nWe do not collect, share, or sell any personal information of users.\n\n3. Data Security:\nAll game data is stored securely directly on your mobile device."

            "terms_content" -> if (isVi)
                "ĐIỀU KHOẢN SỬ DỤNG\n\n1. Quyền sở hữu:\nTrò chơi Block Puzzle được cung cấp miễn phí cho mục đích giải trí cá nhân.\n\n2. Quy định chơi:\nNgười dùng không được can thiệp, gian lận hoặc chỉnh sửa mã nguồn của ứng dụng.\n\n3. Thay đổi điều khoản:\nChúng tôi có quyền cập nhật các điều khoản dịch vụ để nâng cao trải nghiệm người dùng."
            else
                "TERMS OF SERVICE\n\n1. Ownership:\nBlock Puzzle is provided free of charge for personal entertainment purposes.\n\n2. Fair Play:\nUsers must not tamper with, cheat, or modify the application source code.\n\n3. Updates:\nWe reserve the right to update these terms to improve user experience."

            "close" -> if (isVi) "ĐÓNG" else "CLOSE"
            "back" -> if (isVi) "QUAY LẠI" else "BACK"

            else -> key
        }
    }
}
