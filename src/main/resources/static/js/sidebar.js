function toggleDisplay(elementId) {
    var element = document.getElementById(elementId);
    if (element.style.display === 'none') {
        element.style.display = 'block';
    } else {
        element.style.display = 'none';
    }
}

document.getElementById("upload-file-button").addEventListener("click", function() {
    toggleDisplay("upload-content");
    document.getElementById("request-signature-form").style.display = 'none';
    document.getElementById("signature-status-content").style.display = 'none';
});

document.getElementById("request-signature-button").addEventListener("click", function() {
    toggleDisplay("request-signature-form");
    document.getElementById("upload-content").style.display = 'none';
    document.getElementById("signature-status-content").style.display = 'none';
});

document.getElementById("summary-button").addEventListener("click", function() {
    toggleDisplay("signature-status-content");
    document.getElementById("upload-content").style.display = 'none';
    document.getElementById("request-signature-form").style.display = 'none';
});


var uploadArea = document.getElementById('upload-area');
var fileInput = document.getElementById('file-input');
var dragText = document.querySelector('.drag-text');

uploadArea.addEventListener('dragover', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
});

uploadArea.addEventListener('dragleave', function() {
    this.style.backgroundColor = 'transparent';
});

uploadArea.addEventListener('drop', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'transparent';
    fileInput.files = e.dataTransfer.files;
    updateFileNameDisplay();
});

document.querySelector('.browse-button').addEventListener('click', function() {
    fileInput.click();
});

fileInput.addEventListener('change', updateFileNameDisplay);

function updateFileNameDisplay() {
    var file = fileInput.files[0];
    if (file) {
        dragText.textContent = "Selected: " + file.name;
    } else {
        dragText.textContent = "Drag & Drop files here or";
    }
}

