package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Task
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_TASKS)

    fun getByProject(projectId: String): Flow<List<Task>> = callbackFlow {
        val l = ref.whereEqualTo("projectId", projectId)
            .addSnapshotListener { s, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(s?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun getByAssignee(employeeId: String): Flow<List<Task>> = callbackFlow {
        val l = ref.whereEqualTo("assigneeId", employeeId)
            .addSnapshotListener { s, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(s?.documents?.mapNotNull { it.toObject(Task::class.java) }
                    ?.sortedBy { it.deadline } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun getAll(): Flow<List<Task>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(task: Task): Result<String> = try {
        val id = if (task.id.isEmpty()) ref.document().id else task.id
        ref.document(id).set(task.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(task: Task): Result<Unit> = try {
        ref.document(task.id).set(task).await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateStatus(taskId: String, status: String): Result<Unit> = try {
        ref.document(taskId).update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(id: String): Task? = try {
        ref.document(id).get().await().toObject(Task::class.java)
    } catch (e: Exception) { null }
}