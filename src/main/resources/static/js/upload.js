function toggleSendTo() {
    const individualField = document.getElementById('individual-field');
    const groupField = document.getElementById('group-field');

    const individualUsername = document.getElementById('username12');
    const groupName = document.getElementById('group-name');
    const groupDropdown = document.getElementById('group-select'); // Get the group dropdown

    if (document.getElementById('individual').checked) {
        individualField.style.display = 'block';
        groupField.style.display = 'none';

        individualUsername.value = document.getElementById('username12').value;
        groupName.value = '';
    } else {
        individualField.style.display = 'none';
        groupField.style.display = 'block';

        // Get the value of the selected option in the group dropdown
        groupName.value = groupDropdown.options[groupDropdown.selectedIndex].text;
        individualUsername.value = '';
    }
}

const groupDropdown = document.getElementById('group-select');
groupDropdown.addEventListener('change', function() {
    const groupName = document.getElementById('group-name');
    groupName.value = this.options[this.selectedIndex].text;
});



function createNewGroup() {
    document.getElementById('new-group-popup').style.display = 'block';
}

function addMember() {
    var memberName = document.getElementById('member-name').value;
    var membersList = document.getElementById('members-list');
    var memberDiv = document.createElement('div');
    memberDiv.className = 'member';
    memberDiv.innerText = memberName;
    membersList.appendChild(memberDiv);
    document.getElementById('member-name').value = ''; // Clear the input field
}

function confirmNewGroup() {
    var groupName = document.getElementById('new-group-name').value;
    var owner = ownerUsername; // Replace with the actual owner's username
    var members = Array.from(document.getElementsByClassName('member')).map(memberDiv => memberDiv.innerText);

    // Create a JSON object to hold the form data
    var data = {
        owner: owner,
        groupname: groupName,
        members: members
    };

    // Send the request to the server
    fetch('/addgroup', {
	    method: 'POST',
	    headers: {
	        'Content-Type': 'application/json',
	    },
	    body: JSON.stringify(data)
	}).then(response => response.text()).then(responseText => {
	    if (responseText === 'Group created successfully') {
	        alert('Group created successfully'); // Show a success message
            location.reload();// Redirect to the appropriate page
	    } else {
	        alert('Failed to create group'); // Show an error message
	    }
	});

}
document.getElementById('file').addEventListener('change', function() {
    document.getElementById('selected-file-name').value = this.value;
});


 