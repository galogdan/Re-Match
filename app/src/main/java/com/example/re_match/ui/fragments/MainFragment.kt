package com.example.re_match.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.re_match.R
import com.example.re_match.databinding.FragmentMainBinding
import com.example.re_match.ui.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        setupTabs()
        setupNavigation()
    }

    private fun setupTabs() {
        val fragmentList = listOf(
            HomeFragment(),
            DiscoverFragment(),
            ChatFragment(),
            ProfileFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Home"
                    tab.setIcon(R.drawable.ic_home_24dp)
                }
                1 -> {
                    tab.text = "Discover"
                    tab.setIcon(R.drawable.ic_search_24dp)
                }
                2 -> {
                    tab.text = "Chat"
                    tab.setIcon(R.drawable.ic_chat_24dp)
                }
                3 -> {
                    tab.text = "Profile"
                    tab.setIcon(R.drawable.ic_person_24dp)
                }
            }
        }.attach()
    }

    private fun setupNavigation() {
        childFragmentManager.setFragmentResultListener("NAVIGATE_TO_EDIT_PROFILE", viewLifecycleOwner) { _, _ ->
            navController.navigate(R.id.action_mainFragment_to_editProfileFragment)
        }

        childFragmentManager.setFragmentResultListener("NAVIGATE_TO_LOGIN_PAGE", viewLifecycleOwner) { _, _ ->
            navController.navigate(R.id.action_mainFragment_to_loginFragment)
        }

        parentFragmentManager.setFragmentResultListener("PROFILE_UPDATED", viewLifecycleOwner) { _, _ ->
            // Refresh the profile tab
            (childFragmentManager.fragments.find { it is ProfileFragment } as? ProfileFragment)?.refreshProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}