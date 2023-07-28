package com.example.department

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var languageSpinner: Spinner
    lateinit var sharedPreferences: SharedPreferences
    lateinit var departments: MutableSet<String>
    lateinit var selectedLanguages: MutableList<String>
    lateinit var layoutSelectedLanguages: LinearLayout
    lateinit var etEmployeeName: EditText
    lateinit var etEmployeeEmail: EditText
    lateinit var btnAddEmployee: Button
    lateinit var recyclerViewEmployees: RecyclerView
    lateinit var employeesAdapter: EmployeesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        languageSpinner = findViewById(R.id.idLanguageSpinner)
        languageSpinner.onItemSelectedListener = this

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        departments = sharedPreferences.getStringSet("departments", mutableSetOf()) ?: mutableSetOf()

        val adapter: ArrayAdapter<CharSequence> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, departments.toTypedArray())

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Initialize the list of selected languages
        selectedLanguages = mutableListOf()

        layoutSelectedLanguages = findViewById(R.id.layoutSelectedLanguages)
        etEmployeeName = findViewById(R.id.etEmployeeName)
        etEmployeeEmail = findViewById(R.id.etEmployeeEmail)
        btnAddEmployee = findViewById(R.id.btnAddEmployee)
        recyclerViewEmployees = findViewById(R.id.recyclerViewEmployees)

        // Set up RecyclerView for displaying employees
        employeesAdapter = EmployeesAdapter(::removeDepartmentIfEmpty)
        recyclerViewEmployees.layoutManager = LinearLayoutManager(this)
        recyclerViewEmployees.adapter = employeesAdapter

        // Set click listener for the "Add Employee" button
        btnAddEmployee.setOnClickListener {
            onAddEmployeeClick(it)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position > 0) {
            val selectedDepartment = departments.elementAt(position)
            selectedLanguages.add(0, selectedDepartment)
            updateSelectedLanguagesUI()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private fun updateSelectedLanguagesUI() {
        // Clear the previously added TextViews to avoid duplicates
        layoutSelectedLanguages.removeAllViews()

        // Add TextViews for each selected language
        for (language in selectedLanguages) {
            val textView = Button(this)
            textView.text = language
            textView.textSize = 16f
            textView.setPadding(0, 8, 0, 8)

            // Add the TextView to the LinearLayout
            layoutSelectedLanguages.addView(textView)
        }

        // Show the "Add Employee" button only if a valid department is selected
        btnAddEmployee.visibility = if (selectedLanguages.isNotEmpty() && selectedLanguages[0] in departments) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    // Method to handle the click event of the "Add Employee" button
    private fun onAddEmployeeClick(view: View) {
        val department = selectedLanguages.firstOrNull()
        if (department == null || department !in departments) {
            Toast.makeText(this, "Please select a valid department.", Toast.LENGTH_SHORT).show()
            return
        }

        val employeeName = etEmployeeName.text.toString().trim()
        if (employeeName.isEmpty()) {
            Toast.makeText(this, "Please enter the employee name.", Toast.LENGTH_SHORT).show()
            return
        }

        val employeeEmail = etEmployeeEmail.text.toString().trim()
        if (employeeEmail.isEmpty()) {
            Toast.makeText(this, "Please enter the employee email.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the employee with the same email already exists in the department
        if (employeesAdapter.isEmployeeExistsInDepartment(department, employeeEmail)) {
            Toast.makeText(this, "Employee with the same email already exists in $department.", Toast.LENGTH_SHORT).show()
            return
        }

        // Add the employee to the RecyclerView
        val employee = Employee(employeeName, department, employeeEmail)
        employeesAdapter.addEmployee(employee)

        // Clear the input fields
        etEmployeeName.text.clear()
        etEmployeeEmail.text.clear()

        // Hide the keyboard after adding the employee
        hideKeyboard()

        // Reset the spinner selection
        languageSpinner.setSelection(0)

        // Save the updated employees data in shared preferences
        saveEmployeesData()
    }

    // Method to hide the keyboard
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    // Method to save employees data in shared preferences
    private fun saveEmployeesData() {
        val gson = Gson()
        val employeesJson = gson.toJson(employeesAdapter.getEmployeesList())
        sharedPreferences.edit().putString("employees", employeesJson).apply()
    }

    // Method to remove an employee from the list
    fun removeEmployee(position: Int) {
        if (position in 0 until employeesAdapter.itemCount) {
            employeesAdapter.removeEmployee(position)

            // Save the updated employees data in shared preferences
            saveEmployeesData()
        }
    }

    // Method to remove the department from the list if all employees are removed
    private fun removeDepartmentIfEmpty(department: String) {
        departments.remove(department)
        sharedPreferences.edit().putStringSet("departments", departments).apply()

        // Save the updated employees data in shared preferences
        saveEmployeesData()
    }
}
