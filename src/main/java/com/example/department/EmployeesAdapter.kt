package com.example.department

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmployeesAdapter(private val onDepartmentEmpty: (String) -> Unit) : RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder>() {
    private val employeesList: MutableList<Employee> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employeesList[position]
        holder.bind(employee)
    }

    override fun getItemCount(): Int {
        return employeesList.size
    }

    fun addEmployee(employee: Employee) {
        employeesList.add(employee)
        notifyItemInserted(employeesList.size - 1)
    }

    fun removeEmployee(position: Int) {
        if (position in 0 until employeesList.size) {
            val department = employeesList[position].department
            employeesList.removeAt(position)
            notifyItemRemoved(position)

            // Call the onDepartmentEmpty callback if the department becomes empty
            if (employeesList.none { it.department == department }) {
                onDepartmentEmpty(department)
            }
        }
    }

    fun getEmployeesList(): List<Employee> {
        return employeesList
    }

    fun isEmployeeExistsInDepartment(department: String, email: String): Boolean {
        return employeesList.any { it.department == department && it.email == email }
    }

    fun isDepartmentEmpty(department: String): Boolean {
        return employeesList.none { it.department == department }
    }

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewEmployeeName: TextView = itemView.findViewById(R.id.textViewEmployeeName)
        private val textViewEmployeeEmail: TextView = itemView.findViewById(R.id.textViewEmployeeEmail)

        fun bind(employee: Employee) {
            textViewEmployeeName.text = employee.name
            textViewEmployeeEmail.text = employee.email
        }
    }
}
