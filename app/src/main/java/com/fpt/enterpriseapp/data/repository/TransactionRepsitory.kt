package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Transaction
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
class TransactionRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_TRANSACTIONS)

    fun getAll(): Flow<List<Transaction>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Transaction::class.java) }
                ?.sortedByDescending { it.paymentDate } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(tx: Transaction): Result<String> = try {
        val id = if (tx.id.isEmpty()) ref.document().id else tx.id
        ref.document(id).set(tx.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getRevenueByMonth(year: Int, month: Int): Double {
        val start = DateUtil.startOfMonth(year, month)
        val end = DateUtil.endOfMonth(year, month)
        return try {
            val snap = ref.whereGreaterThanOrEqualTo("paymentDate", start)
                .whereLessThanOrEqualTo("paymentDate", end)
                .get().await()
            snap.documents.sumOf { it.getDouble("amount") ?: 0.0 }
        } catch (e: Exception) { 0.0 }
    }

    suspend fun getRevenueThisMonth(): Double {
        val now = java.util.Calendar.getInstance()
        return getRevenueByMonth(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH))
    }
}