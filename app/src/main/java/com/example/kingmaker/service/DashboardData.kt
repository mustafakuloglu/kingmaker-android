package com.example.kingmaker.service

data class DashboardData(
    val activeGoal: String,
    val queuedPeople: List<QueuedPerson>,
    val stats: ContactStats
)

data class QueuedPerson(
    val initials: String,
    val name: String,
    val channel: String,
    val reason: String,
    val position: Int
)

data class ContactStats(
    val total: Int,
    val active: Int,
    val needAttention: Int
)
