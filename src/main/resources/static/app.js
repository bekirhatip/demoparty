const COLORS = [
    '#a8c8ec', '#b8a8d8', '#f5c2e7', '#9dd5f5', '#a8e6f5',
    '#b5e8c4', '#f5b8c4', '#f5e6a8'
  ];
  
  const ICONS = [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
    '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
    '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
    '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
    '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
    '🤧', '🥵', '🥶', '😎', '🤓', '🧐', '👶', '👧', '🧒', '👦',
    '👩', '🧑', '👨', '👵', '🧓', '👴', '👮', '🕵️', '💂', '👷',
    '🤴', '👸', '👳', '👲', '🧕', '🤵', '👰', '🤰', '🤱', '👼',
    '🎅', '🤶', '🦸', '🦹', '🧙', '🧚', '🧛', '🧜', '🧝', '🧞',
    '🧟', '💆', '💇', '🚶', '🏃', '💃', '🕺', '🕴️', '👯', '🧘'
  ];
  
  // Get random item from array
  function getRandomItem(array) {
    return array[Math.floor(Math.random() * array.length)];
  }
  
  // Initialize selection screen
  function initSelectionScreen() {
    const usernameInput = document.getElementById('usernameInput');
    const roomNameInput = document.getElementById('roomNameInput');
    const roomSecretKeyInput = document.getElementById('roomSecretKeyInput');
    const colorOptions = document.querySelectorAll('.color-option');
    const iconOptions = document.querySelectorAll('.icon-option');
    const continueBtn = document.getElementById('continueBtn');
  
    // Get saved user data or generate random defaults
    const savedData = sessionStorage.getItem('userData');
    let userData;
  
    if (savedData) {
      userData = JSON.parse(savedData);
    } else {
      // Generate random defaults for first-time users
      userData = {
        username: `User${Math.floor(Math.random() * 10000)}`,
        roomName: "main",
        roomSecretKey: "",
        color: getRandomItem(COLORS),
        icon: getRandomItem(ICONS),
        session_id: null
      };
    }
  
    // Set initial values
    usernameInput.value = userData.username;
    roomNameInput.value = userData.roomName;
    roomSecretKeyInput.value = userData.roomSecretKey;
    selectColor(userData.color);
    selectIcon(userData.icon);
  
    // Color selection
    colorOptions.forEach(option => {
      option.addEventListener('click', () => {
        const color = option.getAttribute('data-color') || option.style.backgroundColor;
        selectColor(color);
        userData.color = color;
        sessionStorage.setItem('userData', JSON.stringify(userData));
      });
    });
  
    // Icon selection
    iconOptions.forEach(option => {
      option.addEventListener('click', () => {
        const icon = option.textContent;
        selectIcon(icon);
        userData.icon = icon;
        sessionStorage.setItem('userData', JSON.stringify(userData));
      });
    });
  
    // Username input
    usernameInput.addEventListener('input', (e) => {
      userData.username = e.target.value.trim() || userData.username;
      sessionStorage.setItem('userData', JSON.stringify(userData));
    });
  
    // Continue button
    continueBtn.addEventListener('click', () => {
      const username = usernameInput.value.trim();
      const roomName = roomNameInput.value.trim();
      const roomSecretKey = roomSecretKeyInput.value.trim();
      if (!username) {
        alert('Please enter a username');
        return;
      }
      if (!roomName) {
        alert('Please enter a room Name');
        return;
      }
  
      userData.username = username;
      userData.roomName = roomName;
      userData.roomSecretKey = roomSecretKey;
      const selectedColorOption = document.querySelector('.color-option.selected');
      userData.color = selectedColorOption ? (selectedColorOption.getAttribute('data-color') || selectedColorOption.style.backgroundColor) : userData.color;
      userData.icon = document.querySelector('.icon-option.selected').textContent;
      
      sessionStorage.setItem('userData', JSON.stringify(userData));
      startChat(userData);
    });
  }
  
  function selectColor(color) {
    document.querySelectorAll('.color-option').forEach(option => {
      option.classList.remove('selected');
      // Compare using data attribute or backgroundColor
      const optionColor = option.getAttribute('data-color') || option.style.backgroundColor;
      if (optionColor === color || option.style.backgroundColor === color) {
        option.classList.add('selected');
      }
    });
  }
  
  function selectIcon(icon) {
    document.querySelectorAll('.icon-option').forEach(option => {
      option.classList.remove('selected');
      if (option.textContent === icon) {
        option.classList.add('selected');
      }
    });
  }
  
  // Initialize color options
  function initColorOptions() {
    const colorContainer = document.getElementById('colorOptions');
    COLORS.forEach(color => {
      const div = document.createElement('div');
      div.className = 'color-option';
      div.style.backgroundColor = color;
      div.setAttribute('data-color', color); // Store color as data attribute for easier matching
      colorContainer.appendChild(div);
    });
  }
  
  // Initialize icon options
  function initIconOptions() {
    const iconContainer = document.getElementById('iconOptions');
    ICONS.forEach(icon => {
      const div = document.createElement('div');
      div.className = 'icon-option';
      div.textContent = icon;
      iconContainer.appendChild(div);
    });
  }
  
  // Chat functionality
  let ws = null;
  let username = '';
  let userColor = '';
  let userIcon = '';
  let userSessionId = '';
  let room = 'main';
  let lastMessageUser = null; // Track last message sender for grouping
  
  function startChat(userData) {

    function encryptMessage(message, secretKey) {
      if (!secretKey) return message;

      const encrypted = CryptoJS.AES.encrypt(message, secretKey);
      return "ENC::" + encrypted.toString(); // Base64 string
    }

    function decryptMessage(cipherText, secretKey) {
      if (!cipherText.startsWith("ENC::")) {
        return cipherText;
      }

      try{
        const realCipher = cipherText.replace("ENC::", "");
        const bytes = CryptoJS.AES.decrypt(realCipher, secretKey);
        const message = bytes.toString(CryptoJS.enc.Utf8);
        if (message && message !== ''){
          return message
        }
        else{
          return "!!SecretKeyError!!";
        }
      }catch{
        return "!!SecretKeyError!!";
      }
    }

    username = userData.username;
    room = userData.roomName;
    roomSecretKey = userData.roomSecretKey;
    userColor = userData.color;
    userIcon = userData.icon;
    userSessionId = userData.session_id;
  
    // Hide selection screen and show chat room
    document.getElementById('selectionScreen').style.display = 'none';
    document.getElementById('chatRoom').classList.add('active');
  
    // Update user info in header
    const userAvatar = document.querySelector('.user-info .user-avatar');
    const userEmoji = document.querySelector('.user-info .avatar-emoji');
    const userNameDisplay = document.querySelector('.user-info .user-name');
    userAvatar.style.backgroundColor = userColor;
    userEmoji.textContent = userIcon;
    userNameDisplay.textContent = username;
  
    // Setup mobile users dropdown toggle
    setupMobileUsersDropdown();
  
    // Update connection status
    const connectionStatus = document.getElementById('connectionStatus');
    connectionStatus.textContent = 'Connecting...';
    connectionStatus.className = 'connection-status';
  
    // Establish WebSocket connection
    // Use wss:// for https, ws:// for http
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${location.host}/ws/chat`;
    
    console.log('Connecting to WebSocket:', wsUrl);
    
    try {
      ws = new WebSocket(wsUrl);
  
      ws.onopen = () => {
        console.log('WebSocket connected');
        connectionStatus.textContent = 'Connected';
        connectionStatus.className = 'connection-status connected';
        ws.send(JSON.stringify({ type: 'join', username, room, icon: userIcon, color: userColor }));
      };
  
      ws.onmessage = (e) => {
        const data = JSON.parse(e.data);
  
        switch (data.type) {
          case 'message':
            let msg = data.message;
            // if (roomSecretKey && roomSecretKey !== '') {
            //   msg = decryptMessage(msg, roomSecretKey);
            // }
            msg = decryptMessage(msg, roomSecretKey);
            addMessage(
              data.username, 
              msg, 
              data.color,
              data.icon,
              data.session_id
            );
            break;
          case 'system':
            if (data.session_id) {
  
              const userDataRaw = sessionStorage.getItem('userData');
              const userData = userDataRaw ? JSON.parse(userDataRaw) : {};
          
              username = data.new_username;
              userSessionId = data.session_id;
              userData.session_id = data.session_id;
              userData.username = data.new_username;
              
              sessionStorage.setItem('userData', JSON.stringify(userData));
            }
  
            addSystemMessage(data.message);
            lastMessageUser = null; // Reset on system messages
            break;
          case 'error':
            alert(data.message);
            break;
          case 'user_list':
            renderUsers(data.users);
            break;
        }
      };
  
      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        console.error('WebSocket URL was:', wsUrl);
        connectionStatus.textContent = 'Connection Error';
        connectionStatus.className = 'connection-status error';
        addSystemMessage('Connection error. Please check your network connection.');
        // Show alert for mobile Safari where console might not be visible
        setTimeout(() => {
          alert('WebSocket connection failed. URL: ' + wsUrl + '\n\nPlease ensure:\n1. The server is running\n2. You are on the same network\n3. Firewall allows WebSocket connections');
        }, 500);
      };
  
      ws.onclose = (event) => {
        console.log('WebSocket closed:', event.code, event.reason);
        connectionStatus.textContent = 'Disconnected';
        connectionStatus.className = 'connection-status error';
        if (event.code !== 1000) { // 1000 is normal closure
          addSystemMessage('Connection closed. Code: ' + event.code + '. Please refresh the page.');
        }
      };
    } catch (error) {
      console.error('Failed to create WebSocket:', error);
      connectionStatus.textContent = 'Failed';
      connectionStatus.className = 'connection-status error';
      alert('Failed to establish connection: ' + error.message + '\n\nWebSocket URL: ' + wsUrl);
    }
  
    // Chat form submission
    const form = document.getElementById('chatForm');
    const input = document.getElementById('messageInput');
  
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      let msg = input.value.trim();
      if (roomSecretKey && roomSecretKey !== '') {
        msg = encryptMessage(msg, roomSecretKey);
      }
      if (msg && ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'message', message: msg }));
        input.value = '';
      }
    });
  }
  
  function addMessage(user, text, color, icon, session_id) {
    const chat = document.getElementById('chat');
    const isMine = session_id === userSessionId;
    
    // Check if this is a consecutive message from the same user
    const isConsecutive = lastMessageUser === user;
    lastMessageUser = user;
  
    const div = document.createElement('div');
    div.classList.add('message');
    div.classList.add(isMine ? 'self' : 'other');
    
    // Add class for consecutive messages
    if (isConsecutive) {
      div.classList.add('consecutive');
      const lastMessageEl = chat.lastElementChild;

      if (lastMessageEl && lastMessageEl.classList.contains('message')) {
        lastMessageEl.style.marginBottom = '2px';
      }
    }
  
    // Only show author info if it's not a consecutive message from the same user
    if (!isMine && !isConsecutive) {
      const authorDiv = document.createElement('div');
      authorDiv.classList.add('author');
      
      const iconSpan = document.createElement('span');
      iconSpan.classList.add('author-icon');
      
      // Generate a consistent color and icon for other users based on their username
      if (color && icon) {
        iconSpan.textContent = icon;
        iconSpan.style.backgroundColor = color;
      } else {
        // Generate consistent color/icon based on username hash
        const hash = user.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
        iconSpan.textContent = ICONS[hash % ICONS.length];
        iconSpan.style.backgroundColor = COLORS[hash % COLORS.length];
      }
      
      iconSpan.style.borderRadius = '50%';
      iconSpan.style.width = '20px';
      iconSpan.style.height = '20px';
      iconSpan.style.display = 'inline-flex';
      iconSpan.style.alignItems = 'center';
      iconSpan.style.justifyContent = 'center';
      iconSpan.style.marginRight = '6px';
      iconSpan.style.fontSize = '0.75rem';
      
      authorDiv.appendChild(iconSpan);
      authorDiv.appendChild(document.createTextNode(user));
      div.appendChild(authorDiv);
    }

    const contentDiv = document.createElement('div');
    contentDiv.classList.add('content');
    contentDiv.textContent = text;

    if (text == "!!SecretKeyError!!") {
      contentDiv.classList.add('key-error')
      contentDiv.textContent = "⦸⦸⦸ Secret keys are doesn't match!";
    }
  
    div.appendChild(contentDiv);
  
    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
  }
  
  function addSystemMessage(text) {
    const chat = document.getElementById('chat');
    const div = document.createElement('div');
    div.classList.add('system');
    div.textContent = text;
    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
  }
  
  function renderUsers(users) {
    const usersDiv = document.getElementById('users');
    const usersHeader = document.getElementById('usersHeader');
    
    // Update header with count
    usersHeader.textContent = `Online Users (${users.length})`;
    
    usersDiv.innerHTML = '';
  
    console.log(userSessionId);
    console.log(users);
  
    users.sort((a, b) => {
      if (a.session_id === userSessionId) return -1;
      if (b.session_id === userSessionId) return 1;
      return a.username.localeCompare(b.username);
    });
  
    users.forEach(user => {
      const div = document.createElement('div');
      div.classList.add('user-item');
  
      if (user.username === username) {
        div.classList.add('current-user');
      }
  
      const avatar = document.createElement('div');
      avatar.classList.add('user-item-avatar');
      avatar.style.backgroundColor = user.color;
      const emoji = document.createElement('span');
      emoji.classList.add('avatar-emoji')
      emoji.textContent = user.icon;
      avatar.appendChild(emoji)
  
      const name = document.createElement('div');
      name.classList.add('user-item-name');
      name.textContent = user.username;
  
      div.appendChild(avatar);
      div.appendChild(name);
      usersDiv.appendChild(div);
    });
    
    // Update mobile dropdown state if needed
    if (window.innerWidth <= 768 && document.getElementById('chatRoom').classList.contains('active')) {
      const usersSidebar = document.querySelector('.users-sidebar');
      const isExpanded = usersSidebar.classList.contains('mobile-expanded');
      if (!isExpanded) {
        usersDiv.style.display = 'none';
      }
    }
  }
  
  // Setup mobile users dropdown toggle
  function setupMobileUsersDropdown() {
    const usersHeader = document.getElementById('usersHeader');
    const usersDiv = document.getElementById('users');
    const usersSidebar = document.querySelector('.users-sidebar');
    
    if (!usersHeader || !usersDiv || !usersSidebar) return;
    
    // Remove existing listeners
    const newHeader = usersHeader.cloneNode(true);
    usersHeader.parentNode.replaceChild(newHeader, usersHeader);
    
    if (window.innerWidth <= 768) {
      // Hide users list by default on mobile
      usersDiv.style.display = 'none';
      usersSidebar.classList.add('mobile-collapsed');
      usersSidebar.classList.remove('mobile-expanded');
      
      // Toggle on header click
      const header = document.getElementById('usersHeader');
      header.style.cursor = 'pointer';
      header.addEventListener('click', (e) => {
        e.stopPropagation();
        const isHidden = usersDiv.style.display === 'none';
        if (isHidden) {
          usersDiv.style.display = 'block';
          usersSidebar.classList.remove('mobile-collapsed');
          usersSidebar.classList.add('mobile-expanded');
        } else {
          usersDiv.style.display = 'none';
          usersSidebar.classList.remove('mobile-expanded');
          usersSidebar.classList.add('mobile-collapsed');
        }
      });
    } else {
      // Desktop: always show
      usersDiv.style.display = 'block';
      usersSidebar.classList.remove('mobile-collapsed', 'mobile-expanded');
      const header = document.getElementById('usersHeader');
      if (header) header.style.cursor = 'default';
    }
  }
  
  const header = document.querySelector('.chat-header');
  const sidebar = document.querySelector('.users-sidebar');
  
  if (window.visualViewport) {
      window.visualViewport.addEventListener('resize', () => {
          const viewportHeight = window.visualViewport.height;
          const windowHeight = window.innerHeight;
  
          const keyboardOpen = viewportHeight < windowHeight - 100;
  
          if (keyboardOpen) {
              header.classList.add('keyboard-open');
              sidebar.classList.add('keyboard-open');
          } else {
              header.classList.remove('keyboard-open');
              sidebar.classList.remove('keyboard-open');
          }
      });
  }

  // Handle window resize
  window.addEventListener('resize', () => {
    if (document.getElementById('chatRoom').classList.contains('active')) {
      setupMobileUsersDropdown();
    }
  });
  
  // Initialize app
  document.addEventListener('DOMContentLoaded', () => {
    initColorOptions();
    initIconOptions();
    initSelectionScreen();
  });
