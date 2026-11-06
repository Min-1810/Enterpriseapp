package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Customer
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_CUSTOMERS)

    fun getAll(): Flow<List<Customer>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Customer::class.java) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(c: Customer): Result<String> = try {
        val id = if (c.id.isEmpty()) ref.document().id else c.id
        ref.document(id).set(c.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(c: Customer): Result<Unit> = try {
        ref.document(c.id).set(c).await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(id: String): Customer? = try {
        ref.document(id).get().await().toObject(Customer::class.java)
    } catch (e: Exception) { null }
}