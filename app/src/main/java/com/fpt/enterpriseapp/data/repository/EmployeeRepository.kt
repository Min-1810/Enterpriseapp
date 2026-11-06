package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_EMPLOYEES)

    fun getAll(): Flow<List<Employee>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Employee::class.java) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(e: Employee): Result<String> = try {
        val id = if (e.id.isEmpty()) ref.document().id else e.id
        ref.document(id).set(e.copy(id = id)).await()
        Result.success(id)
    } catch (ex: Exception) { Result.failure(ex) }

    suspend fun update(e: Employee): Result<Unit> = try {
        ref.document(e.id).set(e).await(); Result.success(Unit)
    } catch (ex: Exception) { Result.failure(ex) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (ex: Exception) { Result.failure(ex) }

    suspend fun getById(id: String): Employee? = try {
        ref.document(id).get().await().toObject(Employee::class.java)
    } catch (e: Exception) { null }
}