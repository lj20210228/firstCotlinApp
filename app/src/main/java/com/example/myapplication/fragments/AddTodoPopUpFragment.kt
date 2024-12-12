package com.example.myapplication.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.FragmentAddTodoPopUpBinding
import com.example.myapplication.utils.Task
import com.example.myapplication.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*




class AddTodoPopUpFragment : DialogFragment() {

    private lateinit var binding: FragmentAddTodoPopUpBinding
    private lateinit var listener:DialogNextBtnListener
    private var toDoData:ToDoData?=null
    private val myCalendar=Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
          binding= FragmentAddTodoPopUpBinding.inflate(inflater,container,false)
          return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments !=null){
            val name=arguments?.getString("name").toString()
            val date=arguments?.getString("date").toString()
            val task= Task(name,date)
            toDoData=
                ToDoData(arguments?.getString("taskId").toString(),task)
            binding.todoEt.setText(task.name)
            binding.todoDate.setText(task.date)
        }

        registerEvents()
    }
    private fun registerEvents(){
        binding.todoDate.setOnClickListener{
            val datePicker=DatePickerDialog.OnDateSetListener{view,year,month,dayOfMonth->
                myCalendar.set(Calendar.YEAR,year)
                myCalendar.set(Calendar.MONTH,month)
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                updateLabel(myCalendar)

            }
            DatePickerDialog(requireContext(),datePicker,myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.todoNextBtn.setOnClickListener{

            val newName=binding.todoEt.text.toString()
            val newDate=binding.todoDate.text.toString()
            if(newDate.isNotEmpty()&&newDate.isNotEmpty()){
                if(toDoData==null){
                    listener.onSaveTask(newName,binding.todoEt,newDate,binding.todoDate)
                }else{
                    val newTask=Task(newName,newDate)
                    toDoData?.task=newTask
                    listener.onUpdateTask(toDoData!!,binding.todoEt,binding.todoDate)
                }
            }else{
                Toast.makeText(context,"Please type some task",Toast.LENGTH_SHORT).show()
            }

        }
        binding.todoClose.setOnClickListener{dismiss()}
    }
    private fun updateLabel(myCalendar: Calendar)
    {
        val myFormat="dd-MM-yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.UK)
        binding.todoDate.setText(sdf.format(this.myCalendar.time))
    }
    interface  DialogNextBtnListener{
        fun onSaveTask(name:String,todoEt:TextInputEditText,date:String,todoDate:TextInputEditText)
        fun onUpdateTask(toDoData: ToDoData,todoEt: TextInputEditText,todoDate: TextInputEditText)
    }
    fun setListener(listener:DialogNextBtnListener){
        this.listener=listener
    }
    companion object{
        const val TAG="AddTodoPopUpFragment"
        @JvmStatic
        fun newInstance(taskId:String,name:String,date:String)=AddTodoPopUpFragment().apply {
            arguments=Bundle().apply {
                putString("taskId",taskId)
                putString("name",name)
                putString("date",date)
            }
        }
    }





}