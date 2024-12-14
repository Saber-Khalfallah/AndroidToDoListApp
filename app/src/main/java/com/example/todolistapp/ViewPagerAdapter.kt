package com.example.todolistapp

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    activity: MainActivity,
    private val userId: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingTasksFragment.newInstance(userId, false)  // Ongoing tasks
            1 -> CompletedTasksFragment.newInstance(userId, true) // Completed tasks
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}
