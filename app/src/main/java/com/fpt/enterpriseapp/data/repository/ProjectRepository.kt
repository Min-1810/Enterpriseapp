package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Project
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val ref = firestore.collection(Constants.COLLECTION_PROJECTS)

    fun getAll(): Flow<List<Project>> = callbackFlow {
        val l = ref.addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.documents?.mapNotNull { it.toObject(Project::class.java) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun add(p: Project): Result<String> = try {
        val id = if (p.id.isEmpty()) ref.document().id else p.id
        ref.document(id).set(p.copy(id = id)).await()
        Result.success(id)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(p: Project): Result<Unit> = try {
        ref.document(p.id).set(p).await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: String): Result<Unit> = try {
        ref.document(id).delete().await(); Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(id: String): Project? = try {
        ref.document(id).get().await().toObject(Project::class.java)
    } catch (e: Exception) { null }

    suspend fun updateProgress(projectId: String) {
        val tasks = firestore.collection(Constants.COLLECTION_TASKS)
            .whereEqualTo("projectId", projectId).get().await()
        val total = tasks.size()
        if (total == 0) return
        val done = tasks.documents.count {
            it.getString("status") == Constants.TASK_DONE
        }
        ref.document(projectId).update("progress", (done * 100) / total).await()
    }
}