document.addEventListener("DOMContentLoaded", function() {
    const initialFile = document.getElementById('file').options[0].text;
    document.getElementById('selected-file-name').value = initialFile;
});

// Toggle between 'Send To Individual' and 'Send To Group'
const toggleSendTo = () => {
    const individualField = document.getElementById('individual-field');
    const groupField = document.getElementById('group-field');
    const individualUsername = document.getElementById('username12');
    const groupName = document.getElementById('group-name');
    const groupDropdown = document.getElementById('group-select');

    if (document.getElementById('individual').checked) {
        individualField.style.display = 'block';
        groupField.style.display = 'none';
        individualUsername.value = individualUsername.value;
        groupName.value = '';
    } else {
        individualField.style.display = 'none';
        groupField.style.display = 'block';
        groupName.value = groupDropdown.options[groupDropdown.selectedIndex].text;
        individualUsername.value = '';
    }
};

// Update group name on dropdown change
const groupDropdown = document.getElementById('group-select');
groupDropdown.addEventListener('change', function () {
    const groupName = document.getElementById('group-name');
    groupName.value = this.options[this.selectedIndex].text;
});

// Display the popup for creating a new group
const createNewGroup = () => {
    document.getElementById('new-group-popup').style.display = 'block';
};

// Add a new member to the group
const addMember = () => {
    const memberName = document.getElementById('member-name').value;
    const membersList = document.getElementById('members-list');
    const memberDiv = document.createElement('div');
    memberDiv.className = 'member';
    memberDiv.innerText = memberName;
    membersList.appendChild(memberDiv);
    document.getElementById('member-name').value = '';
};

// Confirm the creation of a new group
const confirmNewGroup = () => {
    const groupName = document.getElementById('new-group-name').value;
    const owner = ownerUsername;  // Replace with the actual owner's username
    const members = Array.from(document.getElementsByClassName('member')).map(member => member.innerText);

    // Create a JSON object to hold the form data
    const data = {
        owner,
        groupname: groupName,
        members,
    };

    // Send the request to the server
    fetch('/addgroup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
    .then(response => response.text())
    .then(responseText => {
        if (responseText === 'Group created successfully') {
            alert('Group created successfully');
            location.reload();
        } else {
            alert('Failed to create group');
        }
    });
};

// Display selected file name
document.getElementById('file').addEventListener('change', function() {
    document.getElementById('selected-file-name').value = this.options[this.selectedIndex].text;
});

