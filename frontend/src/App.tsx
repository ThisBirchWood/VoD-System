// App.jsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import ClipUpload from './pages/ClipUpload';
import ClipEdit from './pages/ClipEdit';
import Home from './pages/Home';
import {useEffect} from "react";
import MyClips from './pages/MyClips';
import VideoPlayer from "./pages/VideoPlayer.tsx";


function App() {
    useEffect(() => {
        document.title = "VoD System";
    }, []);

    return (
        <Router>
            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Home />} />
                    <Route path="/create" element={<ClipUpload />} />
                    <Route path="/create/:id" element={<ClipEdit />} />
                    <Route path="/my-clips" element={<MyClips />} />
                    <Route path="/video/:id" element={<VideoPlayer />} />
                </Route>
            </Routes>
        </Router>
    );
}

export default App;