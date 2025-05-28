// App.jsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import ClipUpload from './pages/ClipUpload';
import ClipEdit from './pages/ClipEdit';


function App() {
    return (
        <Router>
            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<h1>Main Page</h1>} />
                    <Route path="/create" element={<ClipUpload />} />
                    <Route path="/create/:id" element={<ClipEdit />} />
                </Route>
            </Routes>
        </Router>
    );
}

export default App;