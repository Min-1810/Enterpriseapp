package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Contract
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContractRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_CONTRACTS)

    fun getAll(): Flow<List<Contract>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Contract::class.java) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(c: Contract): Result<String> = try {
        val id = if (c.id.isEmpty()) ref.document().id else c.id
        ref.document(id).set(c.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(c: Contract): Result<Unit> = try {
        ref.document(c.id).set(c).await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(id: String): Contract? = try {
        ref.document(id).get().await().toObject(Contract::class.java)
    } catch (e: Exception) { null }
}