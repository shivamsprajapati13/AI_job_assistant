const API = '/api';
let currentJobDetails = {};
let matchData = {};
let userEmail = '';
let selectedProvider = '';
let availableProviders = [];
let currentScreenshotFile = null;
let currentDraftId = null;
let activeDraft = null;
let cachedProfile = null;
let profileLoadPromise = null;

document.addEventListener('DOMContentLoaded', async () => {
    userEmail = getStoredEmail();
    initNavigation();
    initUpload();
    initProfile();
    initButtons();
    initDraftModal();
    await initAiProviders();
    checkGmailStatus();
    await prefetchProfile();
    handleRoute();
});

async function prefetchProfile() {
    if (!profileLoadPromise) {
        profileLoadPromise = loadProfile({ showPageLoading: false });
    }
    return profileLoadPromise;
}

function handleRoute() {
    const path = window.location.pathname;
    if (path.includes('profile')) { showPage('profile'); openProfilePage(); }
    else if (path.includes('history')) { showPage('history'); loadHistory(); }
    else showPage('apply');
}

async function openProfilePage() {
    setProfileLoading(true);
    await prefetchProfile();
    setProfileLoading(false);
}

function getStoredEmail() {
    return localStorage.getItem('jobapply_user_email') || '';
}

function storeEmail(email) {
    if (email) localStorage.setItem('jobapply_user_email', email);
}

function initNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = link.dataset.page;
            showPage(page);
            history.pushState(null, '', page === 'apply' ? '/' : `/${page}`);
            if (page === 'history') loadHistory();
            if (page === 'profile') openProfilePage();
        });
    });
}

function showPage(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    const pageEl = document.getElementById(`${page}Page`);
    if (pageEl) pageEl.classList.remove('hidden');
    const navLink = document.querySelector(`[data-page="${page}"]`);
    if (navLink) navLink.classList.add('active');
}

function getSelectedProvider() {
    const navSelect = document.getElementById('aiProviderSelect');
    return navSelect ? navSelect.value : selectedProvider;
}

function providerQuery() {
    const provider = getSelectedProvider();
    return provider ? `&provider=${encodeURIComponent(provider)}` : '';
}

function emailQuery() {
    return userEmail ? `?email=${encodeURIComponent(userEmail)}` : '';
}

function emailQueryAmp() {
    return userEmail ? `&email=${encodeURIComponent(userEmail)}` : '';
}

async function initAiProviders() {
    try {
        const [providersRes, defaultRes] = await Promise.all([
            fetch(`${API}/ai/providers`),
            fetch(`${API}/ai/default-provider`)
        ]);

        if (providersRes.ok) {
            availableProviders = await providersRes.json();
        }

        let defaultProvider = 'gemini';
        if (defaultRes.ok) {
            const defaultData = await defaultRes.json();
            defaultProvider = defaultData.provider || defaultProvider;
        }

        populateProviderSelect('aiProviderSelect', defaultProvider);
        populateProviderSelect('preferredAiProvider', defaultProvider);

        const navSelect = document.getElementById('aiProviderSelect');
        if (navSelect) {
            navSelect.addEventListener('change', () => {
                selectedProvider = navSelect.value;
            });
            selectedProvider = navSelect.value;
        }
    } catch (err) {
        console.error('Failed to load AI providers', err);
    }
}

function populateProviderSelect(selectId, selectedValue) {
    const select = document.getElementById(selectId);
    if (!select || !availableProviders.length) return;

    select.innerHTML = availableProviders.map(p => {
        const status = p.available ? '' : ' (not configured)';
        const selected = p.id === selectedValue ? 'selected' : '';
        const disabled = p.available ? '' : 'disabled';
        return `<option value="${p.id}" ${selected} ${disabled}>${p.displayName}${status}</option>`;
    }).join('');

    if (!select.value && availableProviders.length) {
        const firstAvailable = availableProviders.find(p => p.available);
        if (firstAvailable) select.value = firstAvailable.id;
    }
}

function initUpload() {
    const input = document.getElementById('screenshotInput');
    const zone = document.getElementById('uploadZone');
    const preview = document.getElementById('previewImage');
    const extractBtn = document.getElementById('extractBtn');

    zone.addEventListener('click', (e) => {
        if (e.target.closest('.link-btn')) return;
        input.click();
    });
    zone.addEventListener('dragover', (e) => { e.preventDefault(); zone.classList.add('dragover'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('dragover');
        if (e.dataTransfer.files.length) setScreenshotFile(e.dataTransfer.files[0]);
    });

    input.addEventListener('change', () => {
        if (input.files.length) setScreenshotFile(input.files[0]);
    });

    document.addEventListener('paste', (e) => {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                e.preventDefault();
                const file = item.getAsFile();
                if (file) {
                    setScreenshotFile(file);
                    showToast('Image pasted!', 'success');
                }
                break;
            }
        }
    });

    extractBtn.addEventListener('click', extractJobDetails);
}

function setScreenshotFile(file) {
    if (!file.type.startsWith('image/')) {
        showToast('Please use an image file', 'error');
        return;
    }
    currentScreenshotFile = file;

    const input = document.getElementById('screenshotInput');
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    input.files = dataTransfer.files;

    const preview = document.getElementById('previewImage');
    const zone = document.getElementById('uploadZone');
    const extractBtn = document.getElementById('extractBtn');
    const reader = new FileReader();
    reader.onload = (e) => {
        preview.src = e.target.result;
        preview.classList.remove('hidden');
        zone.querySelector('.upload-content').classList.add('hidden');
        extractBtn.disabled = false;
    };
    reader.readAsDataURL(file);
}

function clearScreenshotUpload() {
    currentScreenshotFile = null;
    const input = document.getElementById('screenshotInput');
    const preview = document.getElementById('previewImage');
    const zone = document.getElementById('uploadZone');
    const extractBtn = document.getElementById('extractBtn');

    input.value = '';
    preview.src = '';
    preview.classList.add('hidden');
    zone.querySelector('.upload-content').classList.remove('hidden');
    extractBtn.disabled = true;
}

async function extractJobDetails() {
    const file = currentScreenshotFile || document.getElementById('screenshotInput').files[0];
    if (!file) return;

    showLoading('Analyzing hiring post with AI...');
    const formData = new FormData();
    formData.append('screenshot', file);

    try {
        const res = await fetch(
            `${API}/extract?email=${encodeURIComponent(userEmail)}${providerQuery()}`,
            { method: 'POST', body: formData }
        );
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || 'Extraction failed');
        }
        const data = await res.json();
        currentJobDetails = data;
        populateJobForm(data);
        document.getElementById('stepReview').classList.remove('hidden');
        updateProfileBanner();
        clearScreenshotUpload();
        showToast('Job details extracted successfully!', 'success');
    } catch (err) {
        showToast(err.message || 'Failed to extract job details.', 'error');
    } finally {
        hideLoading();
    }
}

function populateJobForm(data) {
    document.getElementById('companyName').value = data.companyName || '';
    document.getElementById('jobTitle').value = data.jobTitle || '';
    document.getElementById('requiredExperience').value = data.requiredExperience || '';
    document.getElementById('location').value = data.location || '';
    document.getElementById('recruiterEmail').value = data.recruiterEmail || '';
    document.getElementById('requiredSkills').value = (data.requiredSkills || []).join(', ');
    document.getElementById('applicationInstructions').value = data.applicationInstructions || '';
}

function getJobDetailsFromForm() {
    return {
        companyName: document.getElementById('companyName').value,
        jobTitle: document.getElementById('jobTitle').value,
        requiredExperience: document.getElementById('requiredExperience').value,
        location: document.getElementById('location').value,
        recruiterEmail: document.getElementById('recruiterEmail').value,
        requiredSkills: document.getElementById('requiredSkills').value.split(',').map(s => s.trim()).filter(Boolean),
        applicationInstructions: document.getElementById('applicationInstructions').value,
        highlightedKeywords: currentJobDetails.highlightedKeywords || []
    };
}

function initButtons() {
    document.getElementById('analyzeBtn').addEventListener('click', analyzeMatch);
    document.getElementById('generateEmailBtn').addEventListener('click', generateEmail);
    document.getElementById('sendEmailBtn').addEventListener('click', () => sendApplication());
    document.getElementById('saveDraftBtn').addEventListener('click', saveDraft);
    document.getElementById('saveProfileBtn').addEventListener('click', saveProfile);
}

function initDraftModal() {
    document.getElementById('editFromViewBtn').addEventListener('click', () => openDraftModal(activeDraft, 'edit'));
    document.getElementById('saveDraftEditBtn').addEventListener('click', saveDraftEdit);
    document.getElementById('sendDraftBtn').addEventListener('click', sendDraftFromModal);
    document.getElementById('deleteDraftBtn').addEventListener('click', deleteDraftFromModal);
}

async function analyzeMatch() {
    const jobDetails = getJobDetailsFromForm();
    showLoading('Analyzing resume match...');

    try {
        const res = await fetch(
            `${API}/analyze-match?email=${encodeURIComponent(userEmail)}${providerQuery()}`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(jobDetails)
            }
        );
        if (!res.ok) throw new Error('Analysis failed');
        matchData = await res.json();
        displayMatchAnalysis(matchData);
        showToast('Match analysis complete!', 'success');
    } catch (err) {
        showToast('Failed to analyze match', 'error');
    } finally {
        hideLoading();
    }
}

function displayMatchAnalysis(data) {
    const el = document.getElementById('matchAnalysis');
    el.classList.remove('hidden');

    document.getElementById('matchScoreValue').textContent = data.matchScore;
    document.querySelector('.match-score-ring').style.setProperty('--score', data.matchScore);

    renderSkillTags('matchedSkills', data.matchedSkills, '');
    renderSkillTags('missingSkills', data.missingSkills, 'missing');
    renderSkillTags('highlightedKeywords', data.highlightedKeywords, 'keywords');
    document.getElementById('matchSummary').textContent = data.summary || '';
}

function renderSkillTags(containerId, skills, extraClass) {
    const container = document.getElementById(containerId);
    container.className = `skill-tags ${extraClass}`;
    container.innerHTML = (skills || []).map(s => `<span class="skill-tag">${s}</span>`).join('');
}

async function generateEmail() {
    await prefetchProfile();

    if (!isProfileComplete(cachedProfile)) {
        showToast('Complete your Profile first — AI uses your name, skills, and experience to draft the email.', 'error');
        showPage('profile');
        history.pushState(null, '', '/profile');
        openProfilePage();
        return;
    }

    const jobDetails = getJobDetailsFromForm();
    showLoading('Generating personalized email using your profile...');

    try {
        const res = await fetch(
            `${API}/generate-email?email=${encodeURIComponent(userEmail)}${providerQuery()}`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    jobDetails,
                    matchAnalysis: Object.keys(matchData).length ? matchData : null
                })
            }
        );
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || 'Generation failed');
        }
        const draft = await res.json();
        document.getElementById('emailSubject').value = draft.subject || '';
        document.getElementById('emailBody').value = draft.body || '';
        if (draft.recipientEmail && !document.getElementById('recruiterEmail').value) {
            document.getElementById('recruiterEmail').value = draft.recipientEmail;
        }
        document.getElementById('stepEmail').classList.remove('hidden');
        showToast(`Email drafted for ${cachedProfile.fullName || 'you'} using your profile!`, 'success');
    } catch (err) {
        showToast(err.message || 'Failed to generate email', 'error');
    } finally {
        hideLoading();
    }
}

function isProfileComplete(profile) {
    if (!profile) return false;
    return !!(profile.fullName && (profile.skills || profile.experience || profile.summary || profile.hasResume));
}

function buildSendRequest() {
    const jobDetails = getJobDetailsFromForm();
    return {
        ...jobDetails,
        emailSubject: document.getElementById('emailSubject').value,
        emailBody: document.getElementById('emailBody').value,
        matchScore: matchData.matchScore ?? null,
        missingSkills: matchData.missingSkills || [],
        matchedSkills: matchData.matchedSkills || [],
        highlightedKeywords: matchData.highlightedKeywords || jobDetails.highlightedKeywords
    };
}

function buildDraftRequestFromEdit() {
    return {
        companyName: document.getElementById('editCompany').value,
        jobTitle: document.getElementById('editJobTitle').value,
        location: document.getElementById('editLocation').value,
        recruiterEmail: document.getElementById('editRecipient').value,
        emailSubject: document.getElementById('editSubject').value,
        emailBody: document.getElementById('editBody').value,
        requiredExperience: activeDraft?.requiredExperience || '',
        requiredSkills: activeDraft?.requiredSkills || [],
        applicationInstructions: activeDraft?.applicationInstructions || '',
        matchScore: activeDraft?.matchScore ?? null,
        missingSkills: activeDraft?.missingSkills || [],
        matchedSkills: activeDraft?.matchedSkills || [],
        highlightedKeywords: activeDraft?.highlightedKeywords || []
    };
}

async function sendApplication(draftId = null) {
    const request = buildSendRequest();
    if (!request.recruiterEmail) {
        showToast('Please provide a recruiter email address', 'error');
        return;
    }
    if (!request.emailSubject || !request.emailBody) {
        showToast('Please fill in the email subject and body', 'error');
        return;
    }

    showLoading('Sending application with resume attached...');
    try {
        const url = draftId
            ? `${API}/applications/${draftId}/send${emailQuery()}`
            : `${API}/applications/send${emailQuery()}`;
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Send failed');
        showToast('Application sent with resume attached!', 'success');
        closeDraftModal();
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        hideLoading();
    }
}

async function saveDraft() {
    const request = buildSendRequest();
    showLoading('Saving draft...');
    try {
        const res = await fetch(`${API}/applications/draft${emailQuery()}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Save failed');
        currentDraftId = data.id;
        showToast('Draft saved! View and send it from History when ready.', 'success');
    } catch (err) {
        showToast(err.message || 'Failed to save draft', 'error');
    } finally {
        hideLoading();
    }
}

function initProfile() {
    document.getElementById('resumeInput').addEventListener('change', async (e) => {
        if (!e.target.files.length) return;
        const email = document.getElementById('profileEmail').value.trim();
        if (!email) {
            showToast('Please enter and save your email before uploading a resume', 'error');
            return;
        }
        const formData = new FormData();
        formData.append('resume', e.target.files[0]);
        showLoading('Uploading resume...');
        try {
            const res = await fetch(`${API}/profile/resume?email=${encodeURIComponent(email)}`, {
                method: 'POST', body: formData
            });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || 'Upload failed');
        }
            const data = await res.json();
            userEmail = data.email || email;
            storeEmail(userEmail);
            document.getElementById('resumeStatus').textContent = `Uploaded: ${data.resumeFileName}`;
            showToast('Resume uploaded!', 'success');
        } catch (err) {
            showToast(err.message || 'Failed to upload resume', 'error');
        } finally {
            hideLoading();
        }
    });
}

async function loadProfile(options = {}) {
    const showPageLoading = options.showPageLoading !== false
        && document.getElementById('profilePage')?.classList.contains('hidden') === false;

    if (showPageLoading) setProfileLoading(true);

    try {
        const res = await fetch(`${API}/profile${emailQuery()}`);
        if (!res.ok) throw new Error('Failed to load profile');
        const data = await res.json();
        cachedProfile = data;
        populateProfileForm(data);
        updateProfileBanner();
        return data;
    } catch (err) {
        console.error('Failed to load profile', err);
        if (showPageLoading) {
            const statusEl = document.getElementById('profileLoadStatus');
            if (statusEl) {
                statusEl.textContent = 'Could not load profile. Save your details below.';
                statusEl.classList.remove('hidden');
            }
        }
        return null;
    } finally {
        if (showPageLoading) setProfileLoading(false);
    }
}

function setProfileLoading(loading) {
    const loadingEl = document.getElementById('profileLoading');
    const contentEl = document.getElementById('profileContent');
    if (!loadingEl || !contentEl) return;
    if (loading) {
        loadingEl.classList.remove('hidden');
        contentEl.classList.add('hidden');
    } else {
        loadingEl.classList.add('hidden');
        contentEl.classList.remove('hidden');
    }
}

function updateProfileBanner() {
    let banner = document.getElementById('profileIncompleteBanner');
    const stepReview = document.getElementById('stepReview');
    if (!stepReview || !cachedProfile) return;

    if (!isProfileComplete(cachedProfile)) {
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'profileIncompleteBanner';
            banner.className = 'profile-incomplete-banner';
            stepReview.insertBefore(banner, stepReview.querySelector('.form-grid'));
        }
        banner.textContent = 'Tip: Complete your Profile (name, skills, experience) so AI can draft a personalized email using your details.';
        banner.classList.remove('hidden');
    } else if (banner) {
        banner.classList.add('hidden');
    }
}

function populateProfileForm(data) {
    cachedProfile = data;
    userEmail = data.email || userEmail;
    if (userEmail) storeEmail(userEmail);

    document.getElementById('profileEmail').value = data.email || '';
    document.getElementById('fullName').value = data.fullName || '';
    document.getElementById('currentTitle').value = data.currentTitle || '';
    document.getElementById('phone').value = data.phone || '';
    document.getElementById('linkedinUrl').value = data.linkedinUrl || '';
    document.getElementById('githubUrl').value = data.githubUrl || '';
    document.getElementById('summary').value = data.summary || '';
    document.getElementById('skills').value = data.skills || '';
    document.getElementById('experience').value = data.experience || '';
    document.getElementById('education').value = data.education || '';

    const resumeStatus = document.getElementById('resumeStatus');
    if (data.hasResume) {
        resumeStatus.textContent = `Uploaded: ${data.resumeFileName}`;
    } else {
        resumeStatus.textContent = 'No resume uploaded';
    }

    if (data.preferredAiProvider) {
        const profileSelect = document.getElementById('preferredAiProvider');
        const navSelect = document.getElementById('aiProviderSelect');
        if (profileSelect) profileSelect.value = data.preferredAiProvider;
        if (navSelect) {
            navSelect.value = data.preferredAiProvider;
            selectedProvider = data.preferredAiProvider;
        }
    }

    const statusEl = document.getElementById('profileLoadStatus');
    if (statusEl && data.email) {
        statusEl.textContent = data.fullName
            ? `Editing profile for ${data.fullName} (${data.email})`
            : `Editing profile for ${data.email}`;
        statusEl.classList.remove('hidden');
    }
}

async function saveProfile() {
    const profile = {
        email: document.getElementById('profileEmail').value.trim(),
        fullName: document.getElementById('fullName').value,
        currentTitle: document.getElementById('currentTitle').value,
        phone: document.getElementById('phone').value,
        linkedinUrl: document.getElementById('linkedinUrl').value,
        githubUrl: document.getElementById('githubUrl').value,
        summary: document.getElementById('summary').value,
        skills: document.getElementById('skills').value,
        experience: document.getElementById('experience').value,
        education: document.getElementById('education').value,
        preferredAiProvider: document.getElementById('preferredAiProvider').value
    };

    if (!profile.email) {
        showToast('Please enter your email address', 'error');
        return;
    }

    showLoading('Saving profile...');
    try {
        const res = await fetch(`${API}/profile`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(profile)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Save failed');

        userEmail = data.email;
        storeEmail(userEmail);
        selectedProvider = data.preferredAiProvider || selectedProvider;
        const navSelect = document.getElementById('aiProviderSelect');
        if (navSelect && data.preferredAiProvider) navSelect.value = data.preferredAiProvider;

        populateProfileForm(data);
        updateProfileBanner();
        profileLoadPromise = Promise.resolve(data);
        showToast('Profile saved successfully!', 'success');
    } catch (err) {
        showToast(err.message || 'Failed to save profile', 'error');
    } finally {
        hideLoading();
    }
}

async function loadHistory() {
    try {
        const res = await fetch(`${API}/applications/history${emailQuery()}`);
        if (!res.ok) return;
        const history = await res.json();
        const container = document.getElementById('historyList');
        if (!history.length) {
            container.innerHTML = '<p class="empty-state">No applications yet. Start by applying to a job!</p>';
            return;
        }
        container.innerHTML = history.map(app => `
            <div class="history-item">
                <div class="history-info">
                    <h3>${escapeHtml(app.jobTitle || 'Untitled')} at ${escapeHtml(app.companyName || 'Unknown')}</h3>
                    <p>${escapeHtml(app.location || '')} ${app.recruiterEmail ? '· ' + escapeHtml(app.recruiterEmail) : ''}</p>
                    ${app.status === 'SENT' && app.emailSubject ? `<p class="history-subject">"${escapeHtml(app.emailSubject)}"</p>` : ''}
                    ${app.status === 'SENT' ? `
                    <div class="history-actions">
                        <button class="btn btn-outline btn-sm" onclick="viewSentMessage(${app.id})">View Message</button>
                    </div>` : ''}
                    ${isDraftOrFailed(app.status) ? `
                    <div class="history-actions">
                        <button class="btn btn-outline btn-sm" onclick="viewDraft(${app.id})">View</button>
                        <button class="btn btn-secondary btn-sm" onclick="editDraft(${app.id})">Edit</button>
                        <button class="btn btn-primary btn-sm" onclick="sendDraftById(${app.id})">Send Email</button>
                        <button class="btn btn-outline btn-sm btn-danger-text" onclick="deleteDraft(${app.id})">Delete</button>
                    </div>` : ''}
                </div>
                <div class="history-meta">
                    <span class="status-badge ${app.status}">${app.status}</span>
                    ${app.matchScore ? `<div class="match-pill">${app.matchScore}% match</div>` : ''}
                    <div class="history-date">${formatDate(app.sentAt || app.createdAt)}</div>
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Failed to load history', err);
    }
}

function isDraftOrFailed(status) {
    return status === 'DRAFT' || status === 'FAILED';
}

async function fetchDraft(id) {
    const res = await fetch(`${API}/applications/${id}${emailQuery()}`);
    if (!res.ok) throw new Error('Failed to load draft');
    return res.json();
}

async function viewSentMessage(id) {
    try {
        const app = await fetchDraft(id);
        openDraftModal(app, 'sent');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function viewDraft(id) {
    try {
        const draft = await fetchDraft(id);
        openDraftModal(draft, 'view');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function editDraft(id) {
    try {
        const draft = await fetchDraft(id);
        openDraftModal(draft, 'edit');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function sendDraftById(id) {
    try {
        const draft = await fetchDraft(id);
        openDraftModal(draft, 'edit');
        showToast('Review your email, then click Send Email. Resume will be attached.', 'info');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function openDraftModal(draft, mode) {
    activeDraft = draft;
    const modal = document.getElementById('draftModal');
    const viewMode = document.getElementById('draftViewMode');
    const editMode = document.getElementById('draftEditForm');
    const editFromViewBtn = document.getElementById('editFromViewBtn');
    const saveBtn = document.getElementById('saveDraftEditBtn');
    const sendBtn = document.getElementById('sendDraftBtn');
    const deleteBtn = document.getElementById('deleteDraftBtn');
    const resumeHint = document.getElementById('viewResumeHint');

    const titles = { view: 'View Draft', edit: 'Edit Draft', sent: 'Sent Message' };
    document.getElementById('draftModalTitle').textContent = titles[mode] || 'Application';

    document.getElementById('viewCompany').textContent = draft.companyName || '-';
    document.getElementById('viewJobTitle').textContent = draft.jobTitle || '-';
    document.getElementById('viewLocation').textContent = draft.location || '-';
    document.getElementById('viewRecipient').textContent = draft.recruiterEmail || '-';
    document.getElementById('viewSubject').textContent = draft.emailSubject || '-';
    document.getElementById('viewBody').textContent = draft.emailBody || '(No message content)';

    if (mode === 'sent') {
        resumeHint.textContent = draft.sentAt
            ? `Sent on ${formatDate(draft.sentAt)}. Resume was attached.`
            : 'Resume was attached when this email was sent.';
        viewMode.classList.remove('hidden');
        editMode.classList.add('hidden');
        editFromViewBtn.classList.add('hidden');
        saveBtn.classList.add('hidden');
        sendBtn.classList.add('hidden');
        deleteBtn.classList.add('hidden');
    } else if (mode === 'view') {
        resumeHint.textContent = 'Resume will be attached automatically when you send.';
        viewMode.classList.remove('hidden');
        editMode.classList.add('hidden');
        editFromViewBtn.classList.remove('hidden');
        saveBtn.classList.add('hidden');
        sendBtn.classList.remove('hidden');
        deleteBtn.classList.remove('hidden');
    } else {
        document.getElementById('editCompany').value = draft.companyName || '';
        document.getElementById('editJobTitle').value = draft.jobTitle || '';
        document.getElementById('editLocation').value = draft.location || '';
        document.getElementById('editRecipient').value = draft.recruiterEmail || '';
        document.getElementById('editSubject').value = draft.emailSubject || '';
        document.getElementById('editBody').value = draft.emailBody || '';
        viewMode.classList.add('hidden');
        editMode.classList.remove('hidden');
        editFromViewBtn.classList.add('hidden');
        saveBtn.classList.remove('hidden');
        sendBtn.classList.remove('hidden');
        deleteBtn.classList.remove('hidden');
    }

    modal.classList.remove('hidden');
}

function closeDraftModal() {
    document.getElementById('draftModal').classList.add('hidden');
    activeDraft = null;
}

async function saveDraftEdit() {
    if (!activeDraft) return;
    const request = buildDraftRequestFromEdit();
    showLoading('Saving changes...');
    try {
        const res = await fetch(`${API}/applications/${activeDraft.id}${emailQuery()}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Save failed');
        activeDraft = { ...activeDraft, ...request };
        showToast('Draft updated!', 'success');
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        hideLoading();
    }
}

async function sendDraftFromModal() {
    if (!activeDraft) return;

    const request = document.getElementById('draftEditForm').classList.contains('hidden')
        ? {
            companyName: activeDraft.companyName,
            jobTitle: activeDraft.jobTitle,
            location: activeDraft.location,
            recruiterEmail: activeDraft.recruiterEmail,
            emailSubject: activeDraft.emailSubject,
            emailBody: activeDraft.emailBody,
            requiredExperience: activeDraft.requiredExperience,
            requiredSkills: activeDraft.requiredSkills,
            applicationInstructions: activeDraft.applicationInstructions,
            matchScore: activeDraft.matchScore,
            missingSkills: activeDraft.missingSkills,
            matchedSkills: activeDraft.matchedSkills,
            highlightedKeywords: activeDraft.highlightedKeywords
        }
        : buildDraftRequestFromEdit();

    if (!request.recruiterEmail) {
        showToast('Please provide a recruiter email address', 'error');
        return;
    }
    if (!request.emailSubject || !request.emailBody) {
        showToast('Please fill in the email subject and body', 'error');
        return;
    }

    showLoading('Sending application with resume attached...');
    try {
        const res = await fetch(`${API}/applications/${activeDraft.id}/send${emailQuery()}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Send failed');
        showToast('Email sent with resume attached!', 'success');
        closeDraftModal();
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        hideLoading();
    }
}

async function deleteDraft(id) {
    if (!confirm('Delete this draft? This cannot be undone.')) return;

    showLoading('Deleting draft...');
    try {
        const res = await fetch(`${API}/applications/${id}${emailQuery()}`, { method: 'DELETE' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Delete failed');
        showToast('Draft deleted', 'success');
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        hideLoading();
    }
}

async function deleteDraftFromModal() {
    if (!activeDraft) return;
    if (!confirm('Delete this draft? This cannot be undone.')) return;

    showLoading('Deleting draft...');
    try {
        const res = await fetch(`${API}/applications/${activeDraft.id}${emailQuery()}`, { method: 'DELETE' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Delete failed');
        showToast('Draft deleted', 'success');
        closeDraftModal();
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        hideLoading();
    }
}

async function checkGmailStatus() {
    try {
        const res = await fetch(`${API}/applications/gmail-status`);
        if (!res.ok) return;
        const data = await res.json();
        const badge = document.getElementById('gmailStatus');
        const btn = document.getElementById('connectGmailBtn');
        if (data.connected) {
            badge.textContent = 'Gmail: Connected';
            badge.className = 'gmail-badge connected';
            btn.classList.add('hidden');
        }
    } catch (err) { /* not authenticated */ }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 4000);
}

function showLoading(text) {
    document.getElementById('loadingText').textContent = text || 'Processing...';
    document.getElementById('loadingOverlay').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('hidden');
}
