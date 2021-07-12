// date keys for handling safari date input //
function isIntegerKey(key) {
  return (key >= 48 && key <= 57) || (key >= 96 && key <= 105);
}

function isArrowOrEnterKey(key) {
  return (key >= 37 && key <= 40) || key === 13;
}

function isBackSpaceOrDeleteKey(key) {
  return key === 8 || key === 46;
}

function validateInputDate(event) {
  const key = event.keyCode;
  // only allow integer, backspace, delete, arrow or enter keys
  if (!(isIntegerKey(key) || isBackSpaceOrDeleteKey(key) || isArrowOrEnterKey(key))) {
    event.preventDefault();
  }
}

function deleteDashInputDate(event) {
  const key = event.keyCode;
  const date = event.target.value;
  // remove two chars before the cursor's position if deleting dash character
  if (isBackSpaceOrDeleteKey(key)) {
    const cursorPosition = event.target.selectionStart;
    if (date[cursorPosition - 1] === '-') {
      event.target.value = date.slice(0, cursorPosition - 1);
    }
  }
}

window.formatInputDateOnKeyDown = function formatInputDateOnKeyDown(event) {
  validateInputDate(event);
  deleteDashInputDate(event);
};

window.appendDashInputDate = function appendDashInputDate(event) {
  const date = event.target.value;
  // append dash to date with format yyyy-mm-dd
  if (date.match(/^\d{4}$/) !== null) {
    event.target.value += '-';
  } else if (date.match(/^\d{4}-\d{2}$/) !== null) {
    event.target.value += '-';
  }
};

export default 'test';
