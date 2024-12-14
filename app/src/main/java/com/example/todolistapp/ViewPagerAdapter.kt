package com.example.todolistapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val userId: String
) : FragmentStateAdapter(fragmentActivity) {

    // Define the number of tabs
    private val tabCount = 2

    override fun getItemCount(): Int = tabCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingTasksFragment.newInstance(userId, false)  // Ongoing Tasks
            1 -> CompletedTasksFragment.newInstance(userId, true) // Completed Tasks
            else -> throw IllegalStateException("Invalid tab position: $position")
        }
    }
}
