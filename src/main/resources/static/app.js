const $ = id => document.getElementById(id);
let currentUser = null, allBooks = [], allLoans = [];
const api = async (url, options={}) => {
  const response = await fetch(url, {headers:{'Content-Type':'application/json'}, ...options});
  const data = await response.json().catch(()=>({}));
  if (!response.ok) throw new Error(data.message || data.detail || 'Something went wrong. Please try again.');
  return data;
};
function message(text, success=false){ $('authMessage').textContent=text; $('authMessage').className='message'+(success?' success':''); }
function showAuth(which){ $('loginView').classList.toggle('hidden',which!=='login'); $('registerView').classList.toggle('hidden',which!=='register'); message(''); }
$('showRegister').onclick=()=>showAuth('register'); $('showLogin').onclick=()=>showAuth('login');
$('registerForm').addEventListener('submit',async e=>{
  e.preventDefault(); const password=$('regPassword').value;
  if(password!==$('regConfirm').value){message('Passwords do not match.');return;}
  try{const username=$('regUsername').value.trim(); const result=await api('/api/register',{method:'POST',body:JSON.stringify({name:$('regName').value.trim(),username,password})});
    $('registerForm').reset(); showAuth('login'); $('loginUsername').value=username; message(result.message || 'Registration successful. Please log in.',true);
  }catch(err){message(err.message);}
});
$('loginForm').addEventListener('submit',async e=>{
  e.preventDefault();
  try{currentUser=await api('/api/login',{method:'POST',body:JSON.stringify({username:$('loginUsername').value,password:$('loginPassword').value})});
    $('authShell').classList.add('hidden');$('appShell').classList.remove('hidden');$('userGreeting').textContent=currentUser.name;
    $('dashName').textContent=currentUser.name; document.querySelectorAll('.staff-only').forEach(el=>el.classList.toggle('hidden',!['admin','librarian'].includes(currentUser.role)));
    await refresh(); showPage('dashboard');
  }catch(err){message(err.message);}
});
$('logoutBtn').onclick=()=>{currentUser=null;$('appShell').classList.add('hidden');$('authShell').classList.remove('hidden');$('loginForm').reset();showAuth('login');};
function showPage(name){document.querySelectorAll('.page').forEach(p=>p.classList.add('hidden'));$(name+'Page').classList.remove('hidden');document.querySelectorAll('.nav-btn').forEach(b=>b.classList.toggle('active',b.dataset.page===name));}
document.querySelectorAll('.nav-btn').forEach(b=>b.onclick=()=>showPage(b.dataset.page));
document.querySelectorAll('[data-goto]').forEach(b=>b.onclick=()=>showPage(b.dataset.goto));
async function refresh(){allBooks=await api('/api/books');allLoans=await api('/api/loans?username='+encodeURIComponent(currentUser.username));$('bookCount').textContent=allBooks.length;$('availableCount').textContent=allBooks.filter(b=>b.available).length;$('loanCount').textContent=allLoans.filter(l=>l.status==='Borrowed').length;renderBooks();renderLoans();}
function bookCard(b,manage=false){return `<article class="book-card"><img src="${b.image}" alt="Cover of ${escapeHtml(b.title)}"><div class="book-info"><span class="pill ${b.available?'':'unavailable'}">${b.available?'Available':'Borrowed'}</span><h3>${escapeHtml(b.title)}</h3><p>${escapeHtml(b.author)}<br>${escapeHtml(b.category)}</p>${manage?`<button class="outline" onclick="removeBook(${b.id})">Remove</button>`:`<button class="primary" ${b.available?'':'disabled'} onclick="borrowBook(${b.id})">${b.available?'Borrow book':'Unavailable'}</button>`}</div></article>`;}
function renderBooks(){const query=($('searchBox')?.value||'').toLowerCase();const filtered=allBooks.filter(b=>(b.title+' '+b.author+' '+b.category).toLowerCase().includes(query));$('bookGrid').innerHTML=filtered.map(b=>bookCard(b)).join('')||'<p class="empty">No books match your search.</p>';$('featuredBooks').innerHTML=allBooks.slice(0,3).map(b=>bookCard(b)).join('');$('manageGrid').innerHTML=allBooks.map(b=>bookCard(b,true)).join('');}
function renderLoans(){$('loanRows').innerHTML=allLoans.map(l=>`<tr><td>${escapeHtml(l.bookTitle)}</td><td>${l.status}</td><td>${l.status==='Borrowed'?`<button class="outline" onclick="returnBook(${l.id})">Return</button>`:'—'}</td></tr>`).join('')||'<tr><td colspan="3">You have no loans yet.</td></tr>';}
$('searchBox').addEventListener('input',renderBooks);
async function borrowBook(id){try{await api(`/api/borrow/${id}?username=${encodeURIComponent(currentUser.username)}`,{method:'POST'});await refresh();}catch(e){alert(e.message);}}
async function returnBook(id){try{await api(`/api/return/${id}?username=${encodeURIComponent(currentUser.username)}`,{method:'POST'});await refresh();}catch(e){alert(e.message);}}
async function removeBook(id){try{await api('/api/books/'+id,{method:'DELETE'});await refresh();}catch(e){alert(e.message);}}
$('addBookForm').addEventListener('submit',async e=>{e.preventDefault();try{await api('/api/books',{method:'POST',body:JSON.stringify({title:$('bookTitle').value,author:$('bookAuthor').value,category:$('bookCategory').value})});e.target.reset();await refresh();}catch(err){alert(err.message);}});
function escapeHtml(value){return String(value).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}

// The first screen shown on every fresh visit is the registration page.
showAuth('register');
