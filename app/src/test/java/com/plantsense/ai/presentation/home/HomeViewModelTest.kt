package com.plantsense.ai.presentation.home

import app.cash.turbine.test
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.domain.repository.ImageStorage
import com.plantsense.ai.domain.usecase.GetScanHistoryUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.plantsense.ai.presentation.model.toUiModel

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getScanHistoryUseCase: GetScanHistoryUseCase = mockk()
    private val imageStorage: ImageStorage = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getScanHistoryUseCase() } returns flowOf(emptyList())
        viewModel = HomeViewModel(
            getScanHistoryUseCase,
            imageStorage,
            testDispatcher
        )
    }

    @Test
    fun uiState_initiallyLoadingOrEmpty() = runTest {
        every { getScanHistoryUseCase() } returns flowOf(emptyList())
        val localViewModel = HomeViewModel(
            getScanHistoryUseCase,
            imageStorage,
            testDispatcher
        )
        localViewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            testDispatcher.scheduler.runCurrent()
            assertEquals(HomeUiState.Empty, awaitItem())
        }
    }

    @Test
    fun uiState_hasScans_emitsSuccess() = runTest {
        val sampleScans = listOf(
            ScanHistoryItem(
                id = 1,
                type = ScanType.IDENTIFICATION,
                imageUrl = "image.jpg",
                timestamp = 12345L,
                plantName = "Rose"
            )
        )
        every { getScanHistoryUseCase() } returns flowOf(sampleScans)
        val localViewModel = HomeViewModel(
            getScanHistoryUseCase,
            imageStorage,
            testDispatcher
        )
        localViewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            testDispatcher.scheduler.runCurrent()
            val successState = awaitItem()
            assertTrue(successState is HomeUiState.Success)
            assertEquals(sampleScans.map { it.toUiModel() }, (successState as HomeUiState.Success).scans)
        }
    }

    @Test
    fun onEvent_imagePicked_success_emitsNavigateToIdentify() = runTest {
        coEvery { imageStorage.copyUriToStorage("content://plant") } returns "/data/plant.jpg"

        viewModel.effects.test {
            viewModel.onEvent(HomeEvent.ImagePicked("content://plant"))
            testDispatcher.scheduler.runCurrent()
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.NavigateToIdentify)
            assertEquals("/data/plant.jpg", (effect as HomeEffect.NavigateToIdentify).imagePath)
        }
    }

    @Test
    fun onEvent_diseaseImagePicked_success_emitsNavigateToDisease() = runTest {
        coEvery { imageStorage.copyUriToStorage("content://disease") } returns "/data/disease.jpg"

        viewModel.effects.test {
            viewModel.onEvent(HomeEvent.DiseaseImagePicked("content://disease"))
            testDispatcher.scheduler.runCurrent()
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.NavigateToDisease)
            assertEquals("/data/disease.jpg", (effect as HomeEffect.NavigateToDisease).imagePath)
        }
    }

    @Test
    fun onEvent_imagePicked_failure_emitsShowError() = runTest {
        coEvery { imageStorage.copyUriToStorage("content://invalid") } returns null

        viewModel.effects.test {
            viewModel.onEvent(HomeEvent.ImagePicked("content://invalid"))
            testDispatcher.scheduler.runCurrent()
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.ShowError)
        }
    }
}
