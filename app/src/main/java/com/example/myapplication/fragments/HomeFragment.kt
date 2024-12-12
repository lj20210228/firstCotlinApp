package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MapsActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.utils.Task
import com.example.myapplication.utils.ToDoAdapter
import com.example.myapplication.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment(),AddTodoPopUpFragment.DialogNextBtnListener,ToDoAdapter.ToDoAdapterClicksInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef:DatabaseReference
    private lateinit var  navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUpFragment: AddTodoPopUpFragment? =null
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList:MutableList<ToDoData>
    val CHANNEL_ID="channelID"
    val CHANNEL_NAME="channelName"
    private lateinit var manager:NotificationManager
    val NOTIFICATION_ID=0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
        loadData()
        binding.btButton.setOnClickListener{
            saveData()
        }
        setHasOptionsMenu(true)





    }
    private fun saveData(){
        val insertedText:String=binding.etText.text.toString()
        binding.tvText.text=insertedText
        val sharedPreferences:SharedPreferences=requireContext().getSharedPreferences("sharedPrefs",Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor=sharedPreferences.edit()
        editor.apply(){
            putString("STRING_KEY",insertedText)
            Toast.makeText(context,"Data saved",Toast.LENGTH_SHORT).show()
            binding.etText.setText("")
        }
    }
    private fun loadData()
    {
        val sharedPreferences:SharedPreferences=
            requireContext().getSharedPreferences("sharedPrefs",Context.MODE_PRIVATE)
        val savedString:String?=sharedPreferences.getString("STRING_KEY",null)
        binding.tvText.text=savedString
    }
    private fun getDataFromFirebase(){
        databaseRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val toDoData=taskSnapshot.key?.let{
                        ToDoData(it,taskSnapshot.getValue(Task::class.java)!!)
                        }
                    val task=Task(toDoData?.task!!.name,toDoData.task.date)
                    if(task!=null){
                        mList.add(toDoData!!)
                    }
                    }


                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
            }
        })
}

    private fun init(view:View){
        navController=Navigation.findNavController(view)
        auth=FirebaseAuth.getInstance()
        databaseRef=FirebaseDatabase.getInstance().reference.child("Tasks").child(
            auth.currentUser?.uid.toString()
        )
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager=LinearLayoutManager(context)
        mList= mutableListOf()
        adapter= ToDoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter=adapter
        createNotificationChannel()
    }
    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT).apply {
                lightColor=Color.RED
                enableLights(true)
            }
        }
    }

    private fun registerEvents(){
        binding.addBtnHome.setOnClickListener{
            if(popUpFragment!=null) {
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            }
                popUpFragment = AddTodoPopUpFragment()
                popUpFragment!!.setListener(this)
                popUpFragment!!.show(childFragmentManager,"AddTodoPopUpFragment")


        }
    }

    @SuppressLint("MissingPermission")
    override fun onSaveTask(name: String, todoEt: TextInputEditText,date:String,
    todoDate:TextInputEditText) {
        val task:Task = Task(name,date)
        databaseRef.push().setValue(task).addOnCompleteListener {
            if(it.isSuccessful){
                val notification=NotificationCompat.Builder(requireContext(),CHANNEL_ID)
                    .setContentTitle("Task notification")
                    .setContentText("Task '${name}' was successfully added!")
                    .setSmallIcon(R.drawable.baseline_android_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH).build()
                val notificationManager=NotificationManagerCompat.from(requireContext())
                notificationManager.notify(NOTIFICATION_ID,notification)

            }else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
            todoEt.text=null
            todoDate.text=null
            popUpFragment!!.dismiss()
        }
    }

    override fun onUpdateTask(toDoData: ToDoData, todoEt: TextInputEditText,todoDate: TextInputEditText) {
        val map=HashMap<String,Any>()
        map[toDoData.taskId]=toDoData.task
        databaseRef.updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful)
            {
                Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }

        }
        todoEt.text=null
        todoDate.text=null
        popUpFragment!!.dismiss()
     }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
           if(popUpFragment!=null)
           {
               childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
               popUpFragment=AddTodoPopUpFragment.newInstance(toDoData.taskId,toDoData.task.name!!,
               toDoData.task.date!!)
               popUpFragment!!.setListener(this)
               popUpFragment!!.show(childFragmentManager,AddTodoPopUpFragment.TAG)
           }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)

    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            auth.signOut()
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
            return true
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        binding.gpsBtn.setOnClickListener{
            val intent=Intent(context, MapsActivity::class.java)
            startActivity(intent)
        }
    }


}