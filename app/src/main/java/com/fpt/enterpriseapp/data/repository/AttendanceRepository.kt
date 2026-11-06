package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Attendance
import com.fpt.enterpriseapp.utils.Constants
import com.fpt.enterpriseapp.utils.DateUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_ATTENDANCE)

    suspend fun checkIn(employeeId: String, name: String): Result<Unit> = try {
        val today = DateUtil.todayKey()
        val id = "${employeeId}_$today"
        if (ref.document(id).get().await().exists()) {
            Result.failure(Exception("Đã check-in hôm nay"))
        } else {
            ref.document(id).set(
                Attendance(
                    id = id,
                    employeeId = employeeId,
                    employeeName = name,
                    checkInTime = System.currentTimeMillis(),
                    date = today
                )
            ).await()
            Result.success(Unit)
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun checkOut(employeeId: String): Result<Double> {
        return try {
            val id = "${employeeId}_${DateUtil.todayKey()}"
            val att = ref.document(id).get().await().toObject(Attendance::class.java)
                ?: return Result.failure(Exception("Chưa check-in"))
            if (att.checkOutTime > 0) return Result.failure(Exception("Đã check-out"))
            val out = System.currentTimeMillis()
            val hours = (out - att.checkInTime) / 3600000.0
            ref.document(id).update(mapOf("checkOutTime" to out, "totalHours" to hours)).await()
            Result.success(hours)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getByEmployee(employeeId: String): Flow<List<Attendance>> = callbackFlow {
        val l = ref.whereEqualTo("employeeId", employeeId)
            .addSnapshotListener { s, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(s?.documents?.mapNotNull { it.toObject(Attendance::class.java) }
                    ?.sortedByDescending { it.checkInTime } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun getAll(): Flow<List<Attendance>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Attendance::class.java) }
                ?.sortedByDescending { it.checkInTime } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun getToday(employeeId: String): Attendance? = try {
        ref.document("${employeeId}_${DateUtil.todayKey()}").get().await()
            .toObject(Attendance::class.java)
    } catch (e: Exception) { null }
}