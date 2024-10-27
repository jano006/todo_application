document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('todoForm');
    const nameInput = document.getElementById('name');

    function validateTaskNameLength(name) {
        if (name.length > 100) {
            alert('Task name cannot exceed 100 characters.');
            return false;
        }
        return true;
    }

    form.addEventListener('submit', function (event) {
        const nameValue = nameInput.value.trim();
        if (!validateTaskNameLength(nameValue)) {
            event.preventDefault();
            nameInput.focus();
        }
    });

    function makeTasksEditable() {
        document.querySelectorAll('.editable-task, .edit-icon').forEach(function (element) {
            element.addEventListener('click', function () {
                const taskCell = this.closest('td');
                const currentName = taskCell.querySelector('.editable-task').innerText;

                const inputField = document.createElement('input');
                inputField.type = 'text';
                inputField.value = currentName;
                inputField.classList.add('edit-input');

                taskCell.innerHTML = '';
                taskCell.appendChild(inputField);

                inputField.focus();

                function saveUpdatedName() {
                    const newName = inputField.value.trim();

                    if (!validateTaskNameLength(newName)) {
                        inputField.focus();
                        return;
                    }

                    const todoId = taskCell.parentElement.querySelector('input[name="todoId"]').value;

                    fetch(`/updateName?todoId=${todoId}&newName=${encodeURIComponent(newName)}`, {
                        method: 'POST'
                    }).then(response => {
                        if (response.ok) {
                            taskCell.innerHTML = `<span class="editable-task">${newName}</span>
                                                  <span class="edit-icon">🖉</span>`;

                            makeTasksEditable();
                            makeStatusToggle();
                        } else {
                            alert('Failed to update task name.');
                        }
                    }).catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred while updating the task name.');
                    });
                }

                inputField.addEventListener('blur', saveUpdatedName);

                inputField.addEventListener('keydown', function (event) {
                    if (event.key === 'Enter') {
                        saveUpdatedName();
                    }
                });

                inputField.addEventListener('input', function () {
                    if (inputField.value.length > 100) {
                        alert('Task name cannot exceed 100 characters.');
                        inputField.value = inputField.value.slice(0, 100);
                    }
                });
            });
        });
    }


    function makeStatusToggle() {
        document.querySelectorAll('.status-text, .status-icon').forEach(function (element) {
            element.addEventListener('click', function () {
                const todoId = this.getAttribute('data-todo-id');
                const statusCell = this.closest('td');

                fetch(`/updateIsDoneStatus?todoId=${todoId}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (response.ok) {
                            const statusText = statusCell.querySelector('.status-text');
                            statusText.innerText = statusText.innerText === 'Not finished' ? 'Finished' : 'Not finished';
                        } else {
                            alert('Failed to update status.');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred while updating the status.');
                    });
            });
        });
    }

    function makePriorityEditable() {
        document.querySelectorAll('.priority-select').forEach(function (selectElement) {
            selectElement.addEventListener('change', function () {
                const todoId = this.getAttribute('data-todo-id');
                let newPriority = this.value;

                if (newPriority === 'None') {
                    newPriority = 'null';
                }

                fetch(`/updatePriority?todoId=${todoId}&priority=${encodeURIComponent(newPriority)}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (!response.ok) {
                            alert('Failed to update priority.');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred while updating the priority.');
                    });
            });
        });
    }

    function makeDeadlineEditable() {
        document.querySelectorAll('.deadline-input').forEach(function (inputElement) {
            inputElement.addEventListener('change', function () {
                const todoId = this.getAttribute('data-todo-id');
                const newDate = this.value;

                if (newDate && new Date(newDate) < new Date().setHours(0, 0, 0, 0)) {
                    alert('The deadline cannot be in the past.');
                    this.value = '';
                    return;
                }

                fetch(`/updateDeadline?todoId=${todoId}&newLocalDate=${encodeURIComponent(newDate)}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (!response.ok) {
                            alert('Failed to update deadline.');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred while updating the deadline.');
                    });
            });
        });

        document.querySelectorAll('.clear-deadline-btn').forEach(function (button) {
            button.addEventListener('click', function () {
                const todoId = this.getAttribute('data-todo-id');

                const params = new URLSearchParams();
                params.append('todoId', todoId);

                fetch(`/updateDeadline?${params.toString()}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (!response.ok) {
                            alert('Failed to clear deadline.');
                        } else {
                            document.querySelector(`.deadline-input[data-todo-id="${todoId}"]`).value = '';
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred while clearing the deadline.');
                    });
            });
        });
    }

    makeTasksEditable();
    makeStatusToggle();
    makePriorityEditable();
    makeDeadlineEditable();
});
