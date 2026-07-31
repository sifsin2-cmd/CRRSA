package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ChatMessage::class, FaqItem::class, ServiceMenuItem::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class CrrsaDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun faqDao(): FaqDao
    abstract fun serviceMenuDao(): ServiceMenuDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: CrrsaDatabase? = null

        fun getDatabase(context: Context): CrrsaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrrsaDatabase::class.java,
                    "crrsa_database.db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: CrrsaDatabase) {
                // 1. Initial Welcome Message
                db.chatMessageDao().insertMessage(
                    ChatMessage(
                        sender = "BOT",
                        text = "Hello! Welcome to the official Civil Registration and Residency Service Agency (CRRSA) AI Assistant. How may I help you today? You can inquire about Residency IDs, Birth Certificates, Marriage/Divorce registration, fees, or document requirements.\n\nሰላም! ወደ ሲቪል ምዝገባና የነዋሪነት አገልግሎት ኤጀንሲ እንኳን በደህና መጡ። በምን ልርዳዎት?",
                        language = "EN"
                    )
                )

                // 2. Initial FAQs
                val initialFaqs = listOf(
                    FaqItem(
                        category = "Residency",
                        questionEn = "What documents are required for a New Residency ID?",
                        questionAm = "ለአዲስ የነዋሪነት መታወቂያ ምን ዓይነት ማስረጃዎች ያስፈልጋሉ?",
                        answerEn = "1. Kebele Recommendation Letter or Transfer Certificate\n2. House Rent Agreement or Property Ownership Document\n3. 2 Recent Passport-size Photos\n4. Valid Birth Certificate or Previous Identification",
                        answerAm = "1. የቀበሌ የድጋፍ ደብዳቤ ወይም የዝውውር ማስረጃ\n2. የቤት ኪራይ ውል ወይም የቤት ባለቤትነት ማረጋገጫ\n3. 2 የቅርብ ጊዜ ጉርድ ፎቶግራፍ\n4. የልደት ምስክር ወረቀት ወይም የቀደመ መታወቂያ",
                        isPopular = true,
                        viewsCount = 142
                    ),
                    FaqItem(
                        category = "Birth",
                        questionEn = "How within how many days must a newborn birth be registered?",
                        questionAm = "የሕፃን ልጅ ልደት ከተወለደ ስንት ቀን ውስጥ መመዝገብ አለበት?",
                        answerEn = "Birth registration is mandatory within 90 days of child birth. Registration within 90 days is free of penalty. Late registration incurs a small administrative service fee.",
                        answerAm = "የልደት ምዝገባ ሕፃኑ በተወለደ በ90 ቀናት ውስጥ መከናወን አለበት። በ90 ቀን ውስጥ ማከናወን ከቅጣት ነፃ ነው::",
                        isPopular = true,
                        viewsCount = 98
                    ),
                    FaqItem(
                        category = "Marriage",
                        questionEn = "What are the requirements for Legal Marriage Registration?",
                        questionAm = "ለሕጋዊ የጋብቻ ምዝገባ የሚያስፈልጉ ቅድመ ሁኔታዎች ምንድን ናቸው?",
                        answerEn = "1. Both spouses present with valid Residency IDs\n2. Single status confirmation certificate from respective sub-city/Woreda\n3. 4 Witnesses with valid residency IDs (2 for groom, 2 for bride)\n4. 2 Passport size photos of both spouses",
                        answerAm = "1. ሁለቱም ተጋቢዎች በቦታው ተገኝተው የፀና መታወቂያ ማቅረብ\n2. ከተማዋ/ወረዳው የተሰጠ ያላገባች/ያላገባ ማረጋገጫ\n3. 4 የፀና መታወቂያ ያላቸው ምስክሮች\n4. የሁለቱም ተጋቢዎች ጉርድ ፎቶግራፎች",
                        isPopular = true,
                        viewsCount = 85
                    ),
                    FaqItem(
                        category = "Fees & Offices",
                        questionEn = "What are the official working hours of CRRSA sub-city centers?",
                        questionAm = "የኤጀንሲው የሥራ ሰዓት ስንት ነው?",
                        answerEn = "Monday to Friday: 8:30 AM – 12:30 PM, 1:30 PM – 5:30 PM. Saturday morning services available for urgent certification pick-up: 8:30 AM – 12:00 PM.",
                        answerAm = "ከሰኞ እስከ አርብ: ከጠዋቱ 2:30 - 6:30, ከሰዓት 7:30 - 11:30:: ቅዳሜ ጠዋት: 2:30 - 6:00::",
                        isPopular = false,
                        viewsCount = 64
                    ),
                    FaqItem(
                        category = "Divorce",
                        questionEn = "How do I register a Court Divorce Order?",
                        questionAm = "የፍርድ ቤት የፍቺ ውሳኔ እንዴት ይመዘገባል?",
                        answerEn = "Present final court decree signed by judge, original marriage certificate, valid residency IDs of applicant, and 2 passport photos.",
                        answerAm = "የመጨረሻ የፍርድ ቤት የፍቺ ውሳኔ ግልባጭ፣ ዋናው የጋብቻ ምስክር ወረቀት፣ የፀና መታወቂያ እና 2 ጉርድ ፎቶ ያቅርቡ።",
                        isPopular = false,
                        viewsCount = 31
                    )
                )
                initialFaqs.forEach { db.faqDao().insertFaq(it) }

                // 3. Initial Services Menu
                val initialServices = listOf(
                    ServiceMenuItem(
                        serviceCode = "RESIDENCY_ID",
                        category = "Residency & Verification",
                        titleEn = "New Residency ID Card Application",
                        titleAm = "አዲስ የነዋሪነት መታወቂያ ካርድ",
                        descriptionEn = "Official residency card issuance for new residents or individuals reaching eligible age (18+).",
                        descriptionAm = "ዕድሜያቸው 18 እና ከዚያ በላይ ለሆኑ ወይም አዲስ ለገቡ ነዋሪዎች የሚሰጥ መታወቂያ።",
                        requiredDocumentsEn = "• Kebele recommendation letter\n• House ownership or lease agreement\n• 2 passport photos\n• Previous ID (if applicable)",
                        requiredDocumentsAm = "• የቀበሌ የድጋፍ ደብዳቤ\n• የቤት ባለቤትነት ወይም ኪራይ ውል\n• 2 ጉርድ ፎቶግራፍ",
                        feeEtb = 100.0,
                        processingTimeDays = 3
                    ),
                    ServiceMenuItem(
                        serviceCode = "BIRTH_REG",
                        category = "Civil Registration",
                        titleEn = "Official Birth Registration & Certificate",
                        titleAm = "የልደት ምዝገባ እና የምስክር ወረቀት",
                        descriptionEn = "Vital event registration of child birth with official legal certificate issuing.",
                        descriptionAm = "የሕፃናት ልደት ሕጋዊ ምዝገባና የምስክር ወረቀት አሰጣጥ።",
                        requiredDocumentsEn = "• Health center notification slip\n• Parents' valid residency IDs\n• Marriage certificate of parents (optional)",
                        requiredDocumentsAm = "• የጤና ተቋም የልደት ማረጋገጫ\n• የወላጆች የፀና መታወቂያ\n• የወላጆች የጋብቻ ምስክር ወረቀት",
                        feeEtb = 50.0,
                        processingTimeDays = 1
                    ),
                    ServiceMenuItem(
                        serviceCode = "MARRIAGE_REG",
                        category = "Civil Registration",
                        titleEn = "Legal Marriage Registration & Certificate",
                        titleAm = "የጋብቻ ምዝገባ እና የምስክር ወረቀት",
                        descriptionEn = "Civil marriage solemnization and legal registration certificate.",
                        descriptionAm = "በሕግ ፊት የሚከናወን የጋብቻ ስነ-ስርዓትና የምስክር ወረቀት ምዝገባ።",
                        requiredDocumentsEn = "• Single status certificates\n• Both spouses' IDs\n• 4 Witnesses\n• Photos",
                        requiredDocumentsAm = "• ያላገባ ማረጋገጫ\n• የሁለቱም መታወቂያ\n• 4 ምስክሮች\n• ፎቶግራፍ",
                        feeEtb = 200.0,
                        processingTimeDays = 2
                    ),
                    ServiceMenuItem(
                        serviceCode = "DEATH_REG",
                        category = "Civil Registration",
                        titleEn = "Death Registration & Certificate",
                        titleAm = "የሞት ምዝገባ እና የምስክር ወረቀት",
                        descriptionEn = "Legal record of death event for legal inheritance and administration.",
                        descriptionAm = "የሟች የሞት ሁኔታ ሕጋዊ ምዝገባና ምስክር ወረቀት።",
                        requiredDocumentsEn = "• Medical cause of death certificate\n• Deceased's original ID\n• Applicant's ID & relation proof",
                        requiredDocumentsAm = "• የህክምና ሞት ማረጋገጫ\n• የሟች ዋና መታወቂያ\n• የአመልካች መታወቂያ",
                        feeEtb = 50.0,
                        processingTimeDays = 1
                    ),
                    ServiceMenuItem(
                        serviceCode = "ID_RENEWAL",
                        category = "Residency & Verification",
                        titleEn = "Residency ID Renewal / Replacement",
                        titleAm = "የመታወቂያ እድሳት ወይም ምትክ",
                        descriptionEn = "Renewal of expired residency cards or replacement of lost/damaged cards.",
                        descriptionAm = "የያለፈበት መታወቂያ ማደስ ወይም የጠፋ/የተበላሸ ምትክ ማውጣት።",
                        requiredDocumentsEn = "• Old ID card or Police lost report\n• Rent/ownership renewal slip\n• 2 passport photos",
                        requiredDocumentsAm = "• ነባር መታወቂያ ወይም የፖሊስ ማስረጃ\n• የቤት ውል ማደሻ\n• 2 ጉርድ ፎቶ",
                        feeEtb = 150.0,
                        processingTimeDays = 2
                    )
                )
                initialServices.forEach { db.serviceMenuDao().insertService(it) }

                // 4. App Settings defaults
                db.appSettingDao().saveSetting(AppSetting("app_language", "EN"))
                db.appSettingDao().saveSetting(AppSetting("voice_enabled", "true"))
                db.appSettingDao().saveSetting(AppSetting("dark_mode", "false"))
                db.appSettingDao().saveSetting(AppSetting("upload_dir_perm", "755"))
            }
        }
    }
}
