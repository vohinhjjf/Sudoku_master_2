package com.example.sudoku.view

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.sudoku.R
import com.example.sudoku.game.Cell
import com.example.sudoku.view.custom.BoardView
import com.example.sudoku.viewmodel.SudokuViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), BoardView.OnTouchListener{

    private lateinit var viewModel: SudokuViewModel     //View model

    private lateinit var numberButtons: List<Button>    //Number input buttons

    private lateinit var musicPlayer: MediaPlayer       //Background music player

    private var isMusicPlaying: Boolean = true         //Khac so voi bien isPlaying cua MediaPlayer, bien nay dung de xac dinh su dung nut mute/unmute va trang thai onPause, onResume

    //Override function on create app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(SudokuViewModel::class.java)
        viewModel.sudokuGame.selectCellLiveData.observe(this, Observer { updateSelectCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })
        viewModel.sudokuGame.highlightKeysLiveData.observe(this, Observer { updateHighlightKeys(it) })
        viewModel.sudokuGame.isFinishedLiveData.observe(this, Observer { updateFinishGame(it) })

        numberButtons = listOf(firstButton, secondButton, thirdButton, fourthButton, fifthButton, sixthButton, seventhButton, eighthButton, ninthButton)
        numberButtons.forEachIndexed { index, button -> button.setOnClickListener { viewModel.sudokuGame.handleInput(index + 1 ) } }

        exitButton.setOnClickListener { showExitDialog() }

        musicButton.setOnClickListener { musicToggle() }

        newButton.setOnClickListener { showNewGameDialog(viewModel) }

        hintButton.setOnClickListener { viewModel.sudokuGame.hint() }

        resetButton.setOnClickListener { viewModel.sudokuGame.reset() }

        deleteButton.setOnClickListener { viewModel.sudokuGame.delete() }

        musicPlayer = MediaPlayer.create(applicationContext, R.raw.background_music)
        musicPlayer.isLooping = true
        musicPlayer.setVolume(20F, 20F)
        musicPlayer.start()

        timer.start()
    }

    //Override function on pause
    override fun onPause() {
        super.onPause()
        if (isMusicPlaying)
            musicPlayer.pause()
    }

    //Override function on resume
    override fun onResume() {
        super.onResume()
        if (isMusicPlaying)
            musicPlayer.start()
    }

    //Override function khi nhan nut back tren dien thoai
    override fun onBackPressed() {
        super.onBackPressed()
        showExitDialog()
    }

    //Function update selected cell
    private fun updateSelectCellUI(cell: Pair<Int, Int>?) = cell?.let{
        boardView.updateSelectCellUI(cell.first, cell.second)
    }

    //Function update cells
    private fun updateCells(cells: List<Cell>?) = cells?.let {
        boardView.updateCells(cells)
    }

    //Function update cac number buttons duoc highlight
    private fun updateHighlightKeys(set: Set<Int>?) = set?.let {
        numberButtons.forEachIndexed { index, button ->
            val color = if (set.contains(index + 1)) ContextCompat.getColor(this, R.color.colorPrimary)
                        else Color.LTGRAY
            button.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
    }

    //Function update da finish game hay chua
    private fun updateFinishGame(isFinished: Boolean?) = isFinished?.let {
        if (it) {
            timer.stop()
            showCongratsDialog(viewModel)
        }
    }

    //Function update selected cell khi touch vao cell
    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectCell(row, col)
    }

    //Function khi nhan unmute button
    private fun musicToggle() {
        isMusicPlaying = musicButton.isChecked

        isMusicPlaying = if (!isMusicPlaying) {
            musicPlayer.start()
            true
        } else {
            musicPlayer.pause()
            false
        }
    }

    //Function show dialog khi finish game
    private fun showCongratsDialog(viewModel: SudokuViewModel) {
        val time = (SystemClock.elapsedRealtime() - timer.base) / 1000

        val dialog = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
        dialog.setIcon(R.drawable.finish_icon)
        dialog.setTitle("Congratulations!")
        dialog.setMessage("You have finished the sudoku at $time seconds. Cheers for the hard work!")
        dialog.setPositiveButton("New game") {
            dlg, _ ->
            run {
                dlg.dismiss()
                showNewGameDialog(viewModel)
            }
        }
        dialog.setNegativeButton("Exit") {
            dlg, _ ->
            run {
                dlg.dismiss()
                showExitDialog()
            }
        }

        dialog.show()
    }

    //Function show exit dialog khi nhan nut exit
    private fun showExitDialog() {
        val dialog = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
        dialog.setIcon(R.drawable.exit_game_icon)
        dialog.setTitle("Exit!")
        dialog.setMessage("Exit the game?")
        dialog.setNegativeButton("No") {
                dlg, _ -> dlg.dismiss()
        }
        dialog.setPositiveButton("Yes") {
                _, _ ->
            run {
                timer.base = SystemClock.elapsedRealtime()
                timer.stop()
                musicPlayer.stop()
                musicPlayer.release()
                finish()
                exitProcess(0)
            }
        }
        dialog.show()
    }

    private fun showNewGameDialog(viewModel: SudokuViewModel) {
        val diffNumber: Array<Int> = arrayOf(32, 40, 48)
        val diffName: Array<String> = arrayOf("Easy", "Normal", "Hard")
        var diffChoice = 32

        val dialog = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
        dialog.setIcon(R.drawable.new_game_icon)
        dialog.setTitle("Choose your game difficulty:")
        dialog.setSingleChoiceItems(diffName, 0) {
            _, i -> diffChoice = diffNumber[i]
        }
        dialog.setPositiveButton("OK") {
                _, _ ->
                run {
                    timer.base = SystemClock.elapsedRealtime()
                    timer.stop()
                    viewModel.sudokuGame.newGame(diffChoice)
                    timer.start()
                }
        }
        dialog.setNegativeButton("Cancel") {
            dlg, _ ->  dlg.cancel()
        }

        dialog.show()

    }
}
