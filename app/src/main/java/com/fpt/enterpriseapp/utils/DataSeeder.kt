package com.fpt.enterpriseapp.utils

import com.fpt.enterpriseapp.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

object DataSeeder {
    suspend fun seedData(firestore: FirebaseFirestore) {
        val auth = FirebaseAuth.getInstance()
        val defaultPassword = "123456"

        try {
            // --- 1. TẠO TÀI KHOẢN VÀ NHÂN VIÊN (Có phân bổ Phòng ban) ---
            val employeeDataList = listOf(
                // Admin
                listOf("admin@fpt.com", "Quản trị viên", "ADMIN", "IT"),
                // Managers
                listOf("nam.nguyen@fpt.com", "Nguyễn Văn Nam", "MANAGER", "IT"),
                listOf("bich.tran@fpt.com", "Trần Thị Bích", "MANAGER", "HR"),
                listOf("long.le@fpt.com", "Lê Hoàng Long", "MANAGER", "SALES"),
                // IT Staff
                listOf("dam.vu@fpt.com", "Vũ Đức Đam", "EMPLOYEE", "IT"),
                listOf("ha.hoang@fpt.com", "Hoàng Thu Hà", "EMPLOYEE", "IT"),
                listOf("tri.phan@fpt.com", "Phan Văn Trị", "EMPLOYEE", "IT"),
                listOf("lam.dang@fpt.com", "Đặng Văn Lâm", "EMPLOYEE", "IT"),
                listOf("trai.nguyen@fpt.com", "Nguyễn Trãi", "EMPLOYEE", "IT"),
                listOf("loi.le@fpt.com", "Lê Lợi", "EMPLOYEE", "IT"),
                listOf("kiet.ly@fpt.com", "Lý Thường Kiệt", "EMPLOYEE", "IT"),
                listOf("trung.quang@fpt.com", "Quang Trung", "EMPLOYEE", "IT"),
                listOf("lao.pham@fpt.com", "Phạm Ngũ Lão", "EMPLOYEE", "IT"),
                listOf("kieu.yet@fpt.com", "Yết Kiêu", "EMPLOYEE", "IT"),
                listOf("tuong.da@fpt.com", "Dã Tượng", "EMPLOYEE", "IT"),
                // HR Staff
                listOf("chau.ngo@fpt.com", "Ngô Bảo Châu", "EMPLOYEE", "HR"),
                listOf("xuan.bui@fpt.com", "Bùi Thị Xuân", "EMPLOYEE", "HR"),
                listOf("loan.mai@fpt.com", "Mai Thúc Loan", "EMPLOYEE", "HR"),
                listOf("trung.hai@fpt.com", "Hai Bà Trưng", "EMPLOYEE", "HR"),
                listOf("chan.le@fpt.com", "Lê Chân", "EMPLOYEE", "HR"),
                // Sales Staff
                listOf("hung.do@fpt.com", "Đỗ Mạnh Hùng", "EMPLOYEE", "SALES"),
                listOf("hong.nguyen@fpt.com", "Nguyễn Thị Hồng", "EMPLOYEE", "SALES"),
                listOf("dao.tran@fpt.com", "Trần Hưng Đạo", "EMPLOYEE", "SALES"),
                listOf("tuan.tran@fpt.com", "Trần Quốc Tuấn", "EMPLOYEE", "SALES"),
                listOf("hue.nguyen@fpt.com", "Nguyễn Huệ", "EMPLOYEE", "SALES"),
                // Marketing Staff
                listOf("anh.pham@fpt.com", "Phạm Minh Anh", "EMPLOYEE", "MARKETING"),
                listOf("mai.ly@fpt.com", "Lý Thị Mai", "EMPLOYEE", "MARKETING"),
                listOf("son.truong@fpt.com", "Trương Văn Sơn", "EMPLOYEE", "MARKETING"),
                listOf("sau.vo@fpt.com", "Võ Thị Sáu", "EMPLOYEE", "MARKETING"),
                listOf("hung.phung@fpt.com", "Phùng Hưng", "EMPLOYEE", "MARKETING"),
                listOf("khong.nguyen@fpt.com", "Nguyễn Minh Không", "EMPLOYEE", "MARKETING")
            )

            val employeeMap = mutableMapOf<String, String>()

            for (data in employeeDataList) {
                val email = data[0]
                val name = data[1]
                val role = data[2]
                val dept = data[3]

                try {
                    val result = auth.createUserWithEmailAndPassword(email, defaultPassword).await()
                    val uid = result.user?.uid ?: continue
                    employeeMap[email] = uid

                    val emp = Employee(
                        id = uid, fullName = name, email = email, role = role,
                        department = dept, position = "Staff", phone = "0900000000"
                    )
                    firestore.collection(Constants.COLLECTION_EMPLOYEES).document(uid).set(emp).await()
                    delay(500) // Tránh bị Firebase chặn
                } catch (e: Exception) {
                    val existing = firestore.collection(Constants.COLLECTION_EMPLOYEES)
                        .whereEqualTo("email", email).get().await()
                    if (!existing.isEmpty) {
                        employeeMap[email] = existing.documents[0].id
                    }
                }
            }

            // --- 2. TẠO 10 KHÁCH HÀNG ---
            val customers = listOf(
                Customer(id = "cus1", name = "Công ty TNHH ABC", company = "ABC Corp", email = "contact@abc.vn", phone = "0901234567", assignedEmployeeName = "Lê Hoàng Long"),
                Customer(id = "cus2", name = "Ngân hàng VCB", company = "Vietcombank", email = "info@vcb.vn", phone = "0912345678", assignedEmployeeName = "Lê Hoàng Long"),
                Customer(id = "cus3", name = "Tập đoàn Vingroup", company = "Vingroup", email = "partner@vingroup.vn", phone = "0923456789", assignedEmployeeName = "Đỗ Mạnh Hùng"),
                Customer(id = "cus4", name = "FPT Retail", company = "Long Châu", email = "b2b@longchau.vn", phone = "0934567890", assignedEmployeeName = "Đỗ Mạnh Hùng"),
                Customer(id = "cus5", name = "Viettel Post", company = "Viettel", email = "logistics@viettel.vn", phone = "0945678901", assignedEmployeeName = "Nguyễn Thị Hồng"),
                Customer(id = "cus6", name = "Thế Giới Di Động", company = "MWG", email = "it@thegioididong.vn", phone = "0956789012", assignedEmployeeName = "Trần Quốc Tuấn"),
                Customer(id = "cus7", name = "Hòa Phát Group", company = "Hòa Phát", email = "info@hoaphat.vn", phone = "0967890123", assignedEmployeeName = "Trần Hưng Đạo"),
                Customer(id = "cus8", name = "Vinamilk", company = "Vinamilk", email = "b2b@vinamilk.vn", phone = "0978901234", assignedEmployeeName = "Nguyễn Huệ"),
                Customer(id = "cus9", name = "Masan Group", company = "Masan", email = "partner@masan.vn", phone = "0989012345", assignedEmployeeName = "Đỗ Mạnh Hùng"),
                Customer(id = "cus10", name = "Sabeco", company = "Sabeco", email = "it@sabeco.vn", phone = "0990123456", assignedEmployeeName = "Nguyễn Thị Hồng")
            )
            customers.forEach { firestore.collection(Constants.COLLECTION_CUSTOMERS).document(it.id).set(it).await() }

            // --- 3. HỢP ĐỒNG ---
            val contracts = listOf(
                Contract(id = "hd1", code = "HD-001", customerId = "cus1", customerName = "Công ty TNHH ABC", value = 500000000.0, status = "COMPLETED"),
                Contract(id = "hd2", code = "HD-002", customerId = "cus2", customerName = "Ngân hàng VCB", value = 1200000000.0, status = "ACTIVE")
            )
            contracts.forEach { firestore.collection(Constants.COLLECTION_CONTRACTS).document(it.id).set(it).await() }

            // --- 4. DỰ ÁN ---
            val projects = listOf(
                Project(id = "proj1", name = "App Mobile Banking", progress = 45, status = "ACTIVE", customerName = "Ngân hàng VCB", managerName = "Nguyễn Văn Nam"),
                Project(id = "proj2", name = "Hệ thống ERP", progress = 100, status = "DONE", customerName = "Công ty TNHH ABC", managerName = "Nguyễn Văn Nam")
            )
            projects.forEach { firestore.collection(Constants.COLLECTION_PROJECTS).document(it.id).set(it).await() }

            // --- 5. TASKS ---
            val tasks = listOf(
                // Dự án 1: App Mobile Banking
                Task(id = "t1", projectId = "proj1", projectName = "App Mobile Banking", title = "Thiết kế UI/UX", assigneeName = "Phạm Minh Anh", status = "DONE"),
                Task(id = "t2", projectId = "proj1", projectName = "App Mobile Banking", title = "API Đăng nhập", assigneeName = "Vũ Đức Đam", status = "IN_PROGRESS"),
                Task(id = "t3", projectId = "proj1", projectName = "App Mobile Banking", title = "Tích hợp thanh toán", assigneeName = "Lê Lợi", status = "IN_PROGRESS"),
                Task(id = "t4", projectId = "proj1", projectName = "App Mobile Banking", title = "Màn hình chuyển tiền", assigneeName = "Hoàng Thu Hà", status = "DONE"),
                Task(id = "t5", projectId = "proj1", projectName = "App Mobile Banking", title = "Viết Unit Test", assigneeName = "Trương Văn Sơn", status = "PENDING"),
                Task(id = "t6", projectId = "proj1", projectName = "App Mobile Banking", title = "Báo cáo tuần", assigneeName = "Nguyễn Văn Nam", status = "PENDING"),
                Task(id = "t7", projectId = "proj1", projectName = "App Mobile Banking", title = "Kiểm thử bảo mật", assigneeName = "Lý Thường Kiệt", status = "IN_PROGRESS"),
                Task(id = "t8", projectId = "proj1", projectName = "App Mobile Banking", title = "Tối ưu API", assigneeName = "Phan Văn Trị", status = "PENDING"),
                Task(id = "t31", projectId = "proj1", projectName = "App Mobile Banking", title = "Tư vấn giải pháp", assigneeName = "Đỗ Mạnh Hùng", status = "DONE"),

                // Dự án 2: Hệ thống ERP
                Task(id = "t9", projectId = "proj2", projectName = "Hệ thống ERP", title = "Setup Server", assigneeName = "Nguyễn Văn Nam", status = "DONE"),
                Task(id = "t10", projectId = "proj2", projectName = "Hệ thống ERP", title = "Phân quyền User", assigneeName = "Vũ Đức Đam", status = "DONE"),
                Task(id = "t11", projectId = "proj2", projectName = "Hệ thống ERP", title = "Module Kế toán", assigneeName = "Đặng Văn Lâm", status = "DONE"),
                Task(id = "t12", projectId = "proj2", projectName = "Hệ thống ERP", title = "Module Nhân sự", assigneeName = "Bùi Thị Xuân", status = "DONE"),
                Task(id = "t13", projectId = "proj2", projectName = "Hệ thống ERP", title = "Module Bán hàng", assigneeName = "Trần Hưng Đạo", status = "DONE"),
                Task(id = "t14", projectId = "proj2", projectName = "Hệ thống ERP", title = "Tích hợp Email", assigneeName = "Quang Trung", status = "DONE"),
                Task(id = "t15", projectId = "proj2", projectName = "Hệ thống ERP", title = "Sao lưu dữ liệu", assigneeName = "Nguyễn Trãi", status = "DONE"),
                Task(id = "t16", projectId = "proj2", projectName = "Hệ thống ERP", title = "Tài liệu API", assigneeName = "Phạm Ngũ Lão", status = "PENDING"),
                Task(id = "t17", projectId = "proj2", projectName = "Hệ thống ERP", title = "Test hệ thống", assigneeName = "Yết Kiêu", status = "DONE"),
                Task(id = "t18", projectId = "proj2", projectName = "Hệ thống ERP", title = "Họp nghiệm thu", assigneeName = "Trần Thị Bích", status = "PENDING"),
                Task(id = "t19", projectId = "proj2", projectName = "Hệ thống ERP", title = "Đào tạo User", assigneeName = "Lê Chân", status = "PENDING"),
                Task(id = "t20", projectId = "proj2", projectName = "Hệ thống ERP", title = "Bàn giao", assigneeName = "Hai Bà Trưng", status = "PENDING"),
                Task(id = "t21", projectId = "proj2", projectName = "Hệ thống ERP", title = "Viết báo cáo", assigneeName = "Mai Thúc Loan", status = "DONE"),
                Task(id = "t22", projectId = "proj2", projectName = "Hệ thống ERP", title = "Dọn dẹp Code", assigneeName = "Dã Tượng", status = "DONE"),
                Task(id = "t23", projectId = "proj2", projectName = "Hệ thống ERP", title = "Cấu hình SSL", assigneeName = "Trương Văn Sơn", status = "DONE"),
                Task(id = "t24", projectId = "proj2", projectName = "Hệ thống ERP", title = "Marketing Plan", assigneeName = "Lý Thị Mai", status = "PENDING"),
                Task(id = "t25", projectId = "proj2", projectName = "Hệ thống ERP", title = "Chạy Ads", assigneeName = "Võ Thị Sáu", status = "IN_PROGRESS"),
                Task(id = "t26", projectId = "proj2", projectName = "Hệ thống ERP", title = "Seo Website", assigneeName = "Phùng Hưng", status = "PENDING"),
                Task(id = "t27", projectId = "proj2", projectName = "Hệ thống ERP", title = "Thiết kế Logo", assigneeName = "Nguyễn Minh Không", status = "DONE"),
                Task(id = "t28", projectId = "proj2", projectName = "Hệ thống ERP", title = "Quản lý chi phí", assigneeName = "Lê Hoàng Long", status = "DONE"),
                Task(id = "t29", projectId = "proj2", projectName = "Hệ thống ERP", title = "Chăm sóc KH", assigneeName = "Nguyễn Thị Hồng", status = "IN_PROGRESS"),
                Task(id = "t30", projectId = "proj2", projectName = "Hệ thống ERP", title = "Ký hợp đồng", assigneeName = "Đỗ Mạnh Hùng", status = "DONE")
            )
            tasks.forEach { firestore.collection(Constants.COLLECTION_TASKS).document(it.id).set(it).await() }

            // --- 6. GIAO DỊCH ---
            val currentYear = 2023
            val transactions = listOf(
                Transaction(id = "tx1", contractCode = "HD-001", customerName = "Công ty TNHH ABC", amount = 150000000.0, status = "SUCCESS", paymentDate = DateUtil.startOfMonth(currentYear, 0) + 86400000),
                Transaction(id = "tx2", contractCode = "HD-002", customerName = "Ngân hàng VCB", amount = 300000000.0, status = "SUCCESS", paymentDate = DateUtil.startOfMonth(currentYear, 1) + 86400000)
            )
            transactions.forEach { firestore.collection(Constants.COLLECTION_TRANSACTIONS).document(it.id).set(it).await() }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}