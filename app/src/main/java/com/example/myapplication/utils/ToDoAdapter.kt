package com.example.myapplication.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.EachTodoItemBinding
import com.example.myapplication.fragments.AddTodoPopUpFragment
import kotlin.concurrent.timerTask

class ToDoAdapter(private val list: MutableList<ToDoData>):
RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>(){
    private var listener:ToDoAdapterClicksInterface?=null
    fun setListener(listener:ToDoAdapterClicksInterface){
        this.listener=listener
    }
    inner class ToDoViewHolder(val binding: EachTodoItemBinding):
            RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent:ViewGroup,viewType: Int):
            ToDoViewHolder{
        val binding=EachTodoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ToDoViewHolder(binding)
    }
    override fun getItemCount():Int{
        return  list.size
    }
    interface ToDoAdapterClicksInterface{
        fun onDeleteTaskBtnClicked(toDoData: ToDoData)
        fun onEditTaskBtnClicked(toDoData: ToDoData)
    }
    override fun onBindViewHolder(holder:ToDoViewHolder,posistion:Int){
        with(holder){
            with(list[posistion]){
                binding.todoTask.text=this.task!!.name
                binding.todoDate.text=this.task!!.date
                binding.deleteTask.setOnClickListener{
                    listener?.onDeleteTaskBtnClicked(this)
                }
                binding.editTask.setOnClickListener{
                    listener?.onEditTaskBtnClicked(this)


                }
            }
        }
    }



}
