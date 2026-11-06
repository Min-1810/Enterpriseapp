package com.fpt.enterpriseapp.data.repository

import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val isLoggedIn get() = auth.currentUser != null
    val currentUid get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Employee> = try {
        val r = auth.signInWithEmailAndPassword(email, password).await()
        val uid = r.user?.uid ?: return Result.failure(Exception("UID null"))
        val doc = firestore.collection(Constants.COLLECTION_EMPLOYEES).document(uid).get().await()
        val emp = doc.toObject(Employee::class.java)
        if (emp != null) Result.success(emp)
        else Result.failure(Exception("Không tìm thấy nhân viên"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun register(email: String, password: String, employee: Employee): Result<Unit> = try {
        val r = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = r.user?.uid ?: return Result.failure(Exception("UID null"))
        val newEmp = employee.copy(id = uid, email = email)
        firestore.collection(Constants.COLLECTION_EMPLOYEES).document(uid).set(newEmp).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getCurrentEmployee(): Employee? {
        val uid = currentUid ?: return null
        return try {
            firestore.collection(Constants.COLLECTION_EMPLOYEES).document(uid).get().await()
                .toObject(Employee::class.java)
        } catch (e: Exception) { null }
    }

    fun logout() = auth.signOut()
}