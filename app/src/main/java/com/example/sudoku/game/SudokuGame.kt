package com.example.sudoku.game

import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

class SudokuGame {

    private var selectRow = -1
    private var selectColumn = -1

    private val blockSize = 3       //Block size = size of a block 3x3 = 3 cells on each direction
    private val boardSize = 9       //Board size = size of each direction = 9 cells

    private var board: Board

    private var solution: List<Cell>

    //Dung de nhan select cell data live tu game
    var selectCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    var highlightKeysLiveData = MutableLiveData<Set<Int>>()

    //Dung de tra ve gia tri live da hoan thanh game hay chua
    var isFinishedLiveData = MutableLiveData<Boolean>()

    //--------------------------------------------------------------------------------------------------------
    // INIT AND CELLS & BUTTONS & STATUS FUNCTION REGION
    //Khoi tao cell va live data
    init{
        solution = generateSolution()

        board = Board(boardSize, removeCell(solution, 32))

        selectCellLiveData.postValue(Pair(selectRow, selectColumn))
        cellsLiveData.postValue(board.cells)
        isFinishedLiveData.postValue(false)
    }

    //Function xu li input cho cells
    fun handleInput(number: Int) {
        //Return if 'there's no selected cell' or 'selected cell is a starting cell'
        if (selectRow == -1 || selectColumn == -1) return
        val cell = board.getCell(selectRow, selectColumn)
        if (cell.isStartingCell) return

        cell.value = 0                                          //de tranh truong hop xet o dang chon co gia tri = voi gia tri moi
        cell.isRightValue = isRightCheck(selectRow, selectColumn, number)
        cell.value = number

        cellsLiveData.postValue(board.cells)
        isFinishedLiveData.postValue(isFinished())
    }

    //Update select cell
    fun updateSelectCell(row: Int, col: Int){
        val cell = board.getCell(row, col)
        if (!cell.isStartingCell) {
            selectRow = row
            selectColumn = col
            selectCellLiveData.postValue(Pair(row, col))
        }
    }

    //Function delete
    fun delete() {
        if (selectRow == -1 || selectColumn == -1) return

        val cell = board.getCell(selectRow, selectColumn)
        cell.value = 0
        cell.isRightValue = false

        cellsLiveData.postValue(board.cells)
        isFinishedLiveData.postValue(isFinished())
    }

    //Function reset
    fun reset() {
        for (i in 0 until boardSize*boardSize)
            if (!board.cells[i].isStartingCell) {
                board.cells[i].value = 0
                board.cells[i].isRightValue = false
            }
        cellsLiveData.postValue(board.cells)
    }

    //Function hint
    fun hint() {
        if (selectRow == -1 || selectColumn == -1) return

        val cell = board.getCell(selectRow, selectColumn)
        cell.value = solution[selectRow * boardSize + selectColumn].value
        cell.isRightValue = true

        cellsLiveData.postValue(board.cells)
        isFinishedLiveData.postValue(isFinished())
    }

    //Function new game
    fun newGame(diff: Int) {
        solution = generateSolution()

        board.cells = removeCell(solution, diff)

        selectRow = -1
        selectColumn = -1

        selectCellLiveData.postValue(Pair(selectRow, selectColumn))
        cellsLiveData.postValue(board.cells)
        isFinishedLiveData.postValue(false)
    }

    //--------------------------------------------------------------------------------------------------------
    // GAME LOGIC REGION
    //Function generate solution
    private fun generateSolution(): List<Cell> {
        val newSolution = List(boardSize * boardSize) {
            i  -> Cell( i / 9, i % 9, 0, true)
        }

        fillAll(newSolution)

        return newSolution
    }

    //Function to get possible number for selected cell
    private fun getPossibleNumber(cells: List<Cell>, row: Int, col: Int): Iterable<Int> {
        val numbers = mutableSetOf<Int>()
        numbers.addAll(1..9)

        //Loai cac so da co trong row ra khoi list numbers
        for (c in 0 until boardSize)
            if (cells[row * boardSize + c].value != 0)
                numbers.remove(cells[row * boardSize + c].value)

        //Loai cac so da co trong column ra khoi list numbers
        if (numbers.size > 1)
            for (r in 0 until boardSize)
                if (cells[r * boardSize + col].value != 0)
                    numbers.remove(cells[r * boardSize + col].value)

        //Loai cac so da co trong block 3x3 ra khoi list numbers
        if (numbers.size > 1) {
            val startRow = row / blockSize * blockSize
            val startCol = col / blockSize * blockSize

            for (r in startRow until (startRow + blockSize))
                for (c in startCol until (startCol + blockSize))
                    if (cells[r * boardSize + c].value != 0)
                        numbers.remove(cells[r * boardSize + c].value)
        }

        return numbers.asIterable()
    }

    //Function solve tra ve gia tri true neu solution da generate solve dc
    //Su dung de quy
    private fun solve(cells: List<Cell>): Boolean {
        val test = List(boardSize * boardSize) {
            i -> Cell( i / 9, i % 9, 0, true)
        }

        test.forEach {
            it.value = cells[it.row * boardSize + it.col].value
        }

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
               if (test[row * boardSize + col].value == 0) {
                   val numbers = getPossibleNumber(test, row, col)

                   for (number in numbers){
                       test[row * boardSize + col].value = number
                       if (solve(test))
                           return true
                       test[row * boardSize + col].value = 0
                   }

                   return false
               }
            }
        }

        return true
    }

    //Function thuc hien tat ca cac function fill
    private fun fillAll(cells: List<Cell>) {
        fillDiagonalBlock(cells)
        fillRemainingCell(cells,0, 3)
    }

    //Function de fill cac o theo block 3x3
    private fun fillBlock(cells: List<Cell>, row: Int, col: Int) {
        var number: Int

        for (r in 0 until blockSize)
            for (c in 0 until blockSize) {
                do {
                    number = Random.nextInt(1, 10)
                } while (!isPossibleBlock(cells, row, col, number))

                cells[(row + r) * boardSize + (col + c)].value = number
            }
    }

    //Function fill block theo duong cheo chinh
    //Li do: sudoku khong xet theo duong cheo, vi the fill cac block theo duong cheo truoc se la truong hop nhanh nhat va an toan nhat de tim ra solution
    private fun fillDiagonalBlock(cells: List<Cell>) {
        for (i in 0 until boardSize step blockSize)
            fillBlock(cells, i, i)
    }

    //Function fill cac cell con lai
    //Xet cac block va cell con lai va random fill cell dong thoi xet condition cho den khi tim duoc phuong an toi uu nhat cho cell do
    //Su dung de quy
    private fun fillRemainingCell(cells: List<Cell>, row: Int, col: Int): Boolean {
        var r = row
        var c = col

        if (c >= boardSize && r < boardSize - 1) {
            r += 1
            c = 0
        }

        if (r >= boardSize && c >= boardSize)
            return true

        if (r < blockSize) {
            if (c < blockSize)
                c = blockSize
        } else if (r < boardSize - blockSize) {
            if (c == (r / blockSize) * blockSize)
                c += blockSize
        } else {
            if (c == boardSize - blockSize) {
                r += 1
                c = 0
                if (r >= boardSize)
                    return true
            }
        }

        for (number in 1..9)
            if (isPossibleNumber(cells, r, c, number)) {
                cells[r * boardSize + c].value = number
                if (fillRemainingCell(cells, r, c + 1)) {
                    return true
                }
                cells[r * boardSize + c].value = 0
            }

        return false
    }

    //Function remove number trong cell de tao duoc game hoan chinh
    private fun removeCell(cells: List<Cell>, diff: Int): List<Cell> {
        val removed = List(boardSize * boardSize) {
            i -> Cell( i / 9,  i % 9, cells[(i / 9) * boardSize + (i % 9) ].value, true)
        }

        var numberOfRemoveCell = diff

        while (numberOfRemoveCell > 0) {
            val randomRow = Random.nextInt(0, 9)
            val randomCol = Random.nextInt(0, 9)

            if (removed[randomRow * boardSize + randomCol].value != 0) {
                val number = removed[randomRow * boardSize + randomCol].value
                removed[randomRow * boardSize + randomCol].value = 0
                if (!solve(removed)) {
                    removed[randomRow * boardSize + randomCol].value = number
                } else {
                    removed[randomRow * boardSize + randomCol].isStartingCell = false
                    numberOfRemoveCell--
                }
            }
        }

        return removed
    }

    //Function to check if Number is possible in selected cell
    private fun isPossibleNumber(cells: List<Cell>, row:Int, col: Int, number: Int): Boolean {
        return isPossibleColumn(cells, col, number) && isPossibleRow(cells, row, number) && isPossibleBlock(cells, row, col, number)
    }

    //Function to check if Number is possible on column X
    private fun isPossibleColumn(cells: List<Cell>, col: Int, number: Int): Boolean {
        for (row in 0 until boardSize)
            if (cells[row * boardSize + col].value == number)
                return false
        return true
    }

    //Function to check if Number is possible on row Y
    private fun isPossibleRow(cells: List<Cell>, row: Int, number: Int): Boolean {
        for (col in 0 until boardSize)
            if (cells[row * boardSize + col].value == number)
                return false
        return true
    }

    //Function to check if Number is possible on its Block 3x3
    private fun isPossibleBlock(cells: List<Cell>, row: Int, col: Int, number: Int): Boolean {
        val startRow = row / blockSize * blockSize      //tra ve gia tri Int nen se lay phan nguyen, tuong duong voi Row dau tien cua block can xet
        val startCol = col / blockSize * blockSize      //tuong duong voi Col dau tien cua block can xet

        for (r in startRow until (startRow + blockSize))
                for (c in startCol until (startCol + blockSize))
                    if (cells[r * boardSize + c].value  == number)
                        return false
        return true
    }

    //Function to check if the value input is equal to the value in the solution
    private fun isRightCheck(row: Int, col: Int, number: Int): Boolean {
        if (number == solution[row * boardSize + col].value)
            return true
        return false
    }

    //Function to check if the game is finished or not
    private fun isFinished(): Boolean {
        board.cells.forEach {
            if (!it.isStartingCell) {
                if (!isRightCheck(it.row, it.col, it.value))
                    return false
            }
        }
        return true
    }
}